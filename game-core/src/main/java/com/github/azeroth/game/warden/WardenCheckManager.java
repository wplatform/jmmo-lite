package game;


import java.util.ArrayList;
import java.util.HashMap;

public class WardenCheckManager {
    private static final byte WARDEN_MAX_LUA_CHECK_LENGTH = (byte) 170;
    private final ArrayList<WardenCheck> checks = new ArrayList<>();
    private final HashMap<Integer, byte[]> checkResults = new HashMap<Integer, byte[]>();
    private final ArrayList<SHORT>[] pools = new ArrayList<SHORT>[WardenCheckCategory.max.getValue()];

    private WardenCheckManager() {
        for (var i = 0; i < WardenCheckCategory.max.getValue(); ++i) {
            _pools[i] = new ArrayList<>();
        }
    }

    public static WardenCheckCategory getWardenCheckCategory(WardenCheckType type) {
        return switch (type) {
            case Timing -> WardenCheckCategory.max.getValue();
            case Driver -> WardenCheckCategory.Inject.getValue();
            case Proc -> WardenCheckCategory.max.getValue();
            case LuaEval -> WardenCheckCategory.Lua.getValue();
            case Mpq -> WardenCheckCategory.Modded.getValue();
            case PageA -> WardenCheckCategory.Inject.getValue();
            case PageB -> WardenCheckCategory.Inject.getValue();
            case Module -> WardenCheckCategory.Inject.getValue();
            case Mem -> WardenCheckCategory.Modded.getValue();
            default -> WardenCheckCategory.max.getValue();
        };
    }

    public static WorldCfg getWardenCategoryCountConfig(WardenCheckCategory category) {
        return switch (category) {
            case Inject -> WorldCfg.WardenNumInjectChecks.getValue();
            case Lua -> WorldCfg.WardenNumLuaChecks.getValue();
            case Modded -> WorldCfg.WardenNumClientModChecks.getValue();
            default -> WorldCfg.max.getValue();
        };
    }

    public static boolean isWardenCategoryInWorldOnly(WardenCheckCategory category) {
        return switch (category) {
            case Inject -> false;
            case Lua -> true;
            case Modded -> false;
            default -> false;
        };
    }

    public final short getMaxValidCheckId() {
        return (short) checks.size();
    }

    public final void loadWardenChecks() {
        var oldMSTime = System.currentTimeMillis();

        // Check if Warden is enabled by config before loading anything
        if (!WorldConfig.getBoolValue(WorldCfg.WardenEnabled)) {
            Log.outInfo(LogFilter.Warden, "Warden disabled, loading checks skipped.");

            return;
        }

        //                                         0   1     2     3       4        5       6    7
        var result = DB.World.query("SELECT id, type, data, result, address);

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 Warden checks. DB table `warden_checks` is empty!");

            return;
        }

        int count = 0;

        do {
            var id = result.<SHORT>Read(0);
            var checkType = WardenCheckType.forValue(result.<Byte>Read(1));

            var category = getWardenCheckCategory(checkType);

            if (category == WardenCheckCategory.max) {
                Logs.SQL.error(String.format("Warden check with id %1$s lists check type %2$s in `warden_checks`, which is not supported. Skipped.", id, checkType));

                continue;
            }

            if ((checkType == WardenCheckType.LuaEval) && (id > 9999)) {
                Logs.SQL.error(String.format("Warden Lua check with id %1$s found in `warden_checks`. Lua checks may have four-digit IDs at most. Skipped.", id));

                continue;
            }

            WardenCheck wardenCheck = new WardenCheck();
            wardenCheck.type = checkType;
            wardenCheck.checkId = id;

            if (checkType == WardenCheckType.PageA || checkType == WardenCheckType.PageB || checkType == WardenCheckType.Driver) {
                wardenCheck.data = result.<byte[]>Read(2);
            }

            if (checkType == WardenCheckType.Mpq || checkType == WardenCheckType.Mem) {
                checkResults.put(id, result.<byte[]>Read(3));
            }

            if (checkType == WardenCheckType.Mem || checkType == WardenCheckType.PageA || checkType == WardenCheckType.PageB || checkType == WardenCheckType.Proc) {
                wardenCheck.address = result.<Integer>Read(4);
            }

            if (checkType == WardenCheckType.PageA || checkType == WardenCheckType.PageB || checkType == WardenCheckType.Proc) {
                wardenCheck.length = result.<Byte>Read(5);
            }

            // PROC_CHECK support missing
            if (checkType == WardenCheckType.Mem || checkType == WardenCheckType.Mpq || checkType == WardenCheckType.LuaEval || checkType == WardenCheckType.Driver || checkType == WardenCheckType.module) {
                wardenCheck.str = result.<String>Read(6);
            }

            wardenCheck.comment = result.<String>Read(7);

            if (wardenCheck.comment.isEmpty()) {
                wardenCheck.comment = "Undocumented Check";
            }

            if (checkType == WardenCheckType.LuaEval) {
                if (wardenCheck.str.length() > WARDEN_MAX_LUA_CHECK_LENGTH) {
                    Logs.SQL.error(String.format("Found over-long Lua check for Warden check with id %1$s in `warden_checks`. Max length is %2$s. Skipped.", id, WARDEN_MAX_LUA_CHECK_LENGTH));

                    continue;
                }


                var str = String.format("{0:U4}", id);
                wardenCheck.idStr = str.toCharArray();
            }

            // initialize action with default action from config, this may be overridden later
            wardenCheck.action = WardenActions.forValue(WorldConfig.getIntValue(WorldCfg.WardenClientFailAction));

            _pools[category.getValue()].add(id);
            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s warden checks in %2$s ms", count, time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public final void loadWardenOverrides() {
        var oldMSTime = System.currentTimeMillis();

        // Check if Warden is enabled by config before loading anything
        if (!WorldConfig.getBoolValue(WorldCfg.WardenEnabled)) {
            Log.outInfo(LogFilter.Warden, "Warden disabled, loading check overrides skipped.");

            return;
        }

        //                                              0         1
        var result = DB.characters.query("SELECT wardenId, action FROM warden_action");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 Warden action overrides. DB table `warden_action` is empty!");

            return;
        }

        int count = 0;

        do {
            var checkId = result.<SHORT>Read(0);
            var action = WardenActions.forValue(result.<Byte>Read(1));

            // Check if action value is in range (0-2, see WardenActions enum)
            if (action.getValue() > WardenActions.Ban.getValue()) {
                Log.outError(LogFilter.Warden, String.format("Warden check override action out of range (ID: %1$s, action: %2$s)", checkId, action));
            }
            // Check if check actually exists before accessing the CheckStore vector
            else if (checkId >= checks.size()) {
                Log.outError(LogFilter.Warden, String.format("Warden check action override for non-existing check (ID: %1$s, action: %2$s), skipped", checkId, action));
            } else {
                checks.get(checkId).action = action;
                ++count;
            }
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s warden action overrides in %2$s ms", count, time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public final WardenCheck getCheckData(short id) {
        if (id < checks.size()) {
            return checks.get(id);
        }

        return null;
    }

    public final byte[] getCheckResult(short id) {
        return checkResults.get(id);
    }

    public final ArrayList<SHORT> getAvailableChecks(WardenCheckCategory category) {
        return _pools[category.getValue()];
    }
}
