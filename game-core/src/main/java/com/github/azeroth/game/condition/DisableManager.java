package com.github.azeroth.game.condition;


import com.github.azeroth.game.domain.condition.DisableFlags;
import com.github.azeroth.game.domain.condition.DisableType;

import java.util.ArrayList;
import java.util.HashMap;

public class DisableManager {
    private final HashMap<DisableType, HashMap<Integer, DisableData>> m_DisableMap = new HashMap<DisableType, HashMap<Integer, DisableData>>();

    private DisableManager() {
    }

    public final void loadDisables() {
        var oldMSTime = System.currentTimeMillis();

        // reload case
        m_DisableMap.clear();

        var result = DB.World.query("SELECT sourceType, entry, flags, params_0, params_1 FROM disables");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 disables. DB table `disables` is empty!");

            return;
        }

        int total_count = 0;

        do {
            var type = DisableType.forValue(result.<Integer>Read(0));

            if (type.getValue() >= DisableType.max.getValue()) {
                Logs.SQL.error("Invalid type {0} specified in `disables` table, skipped.", type);

                continue;
            }

            var entry = result.<Integer>Read(1);
            var flags = DisableFlags.forValue(result.<SHORT>Read(2));
            var params_0 = result.<String>Read(3);
            var params_1 = result.<String>Read(4);

            DisableData data = new DisableData();
            data.flags = (short) flags.getValue();

            switch (type) {
                case Spell:
                    if (!(global.getSpellMgr().hasSpellInfo(entry, Difficulty.NONE) || flags.hasFlag(DisableFlags.SPELLDEPRECATEDSPELL))) {
                        Logs.SQL.error("Spell entry {0} from `disables` doesn't exist in dbc, skipped.", entry);

                        continue;
                    }

                    if (flags == 0 || flags.getValue() > DisableFlags.MAXSPELL.getValue()) {
                        Logs.SQL.error("Disable flags for spell {0} are invalid, skipped.", entry);

                        continue;
                    }

                    if (flags.hasFlag(DisableFlags.SPELLMAP)) {
                        var array = new LocalizedString();

                        for (byte i = 0; i < array.length; ) {
                            int id;
                            tangible.OutObject<Integer> tempOut_id = new tangible.OutObject<Integer>();
                            if (tangible.TryParseHelper.tryParseInt(array.get(i++), tempOut_id)) {
                                id = tempOut_id.outArgValue;
                                data.param0.add(id);
                            } else {
                                id = tempOut_id.outArgValue;
                            }
                        }
                    }

                    if (flags.hasFlag(DisableFlags.SPELLAREA)) {
                        var array = new LocalizedString();

                        for (byte i = 0; i < array.length; ) {
                            int id;
                            tangible.OutObject<Integer> tempOut_id2 = new tangible.OutObject<Integer>();
                            if (tangible.TryParseHelper.tryParseInt(array.get(i++), tempOut_id2)) {
                                id = tempOut_id2.outArgValue;
                                data.param1.add(id);
                            } else {
                                id = tempOut_id2.outArgValue;
                            }
                        }
                    }

                    break;
                // checked later
                case Quest:
                    break;
                case Map:
                case LFGMap: {
                    var mapEntry = CliDB.MapStorage.get(entry);

                    if (mapEntry == null) {
                        Logs.SQL.error("Map entry {0} from `disables` doesn't exist in dbc, skipped.", entry);

                        continue;
                    }

                    var isFlagInvalid = false;

                    switch (mapEntry.instanceType) {
                        case MapTypes.Common:
                            if (flags != 0) {
                                isFlagInvalid = true;
                            }

                            break;
                        case MapTypes.Instance:
                        case MapTypes.Raid:
                            if (flags.hasFlag(DisableFlags.DUNGEONSTATUSHEROIC) && global.getDB2Mgr().GetMapDifficultyData(entry, Difficulty.Heroic) == null) {
                                flags = DisableFlags.forValue(flags.getValue() & ~DisableFlags.DUNGEONSTATUSHEROIC.getValue());
                            }

                            if (flags.hasFlag(DisableFlags.DUNGEONSTATUSHEROIC10MAN) && global.getDB2Mgr().GetMapDifficultyData(entry, Difficulty.Raid10HC) == null) {
                                flags = DisableFlags.forValue(flags.getValue() & ~DisableFlags.DUNGEONSTATUSHEROIC10MAN.getValue());
                            }

                            if (flags.hasFlag(DisableFlags.DUNGEONSTATUSHEROIC25MAN) && global.getDB2Mgr().GetMapDifficultyData(entry, Difficulty.Raid25HC) == null) {
                                flags = DisableFlags.forValue(flags.getValue() & ~DisableFlags.DUNGEONSTATUSHEROIC25MAN.getValue());
                            }

                            if (flags == 0) {
                                isFlagInvalid = true;
                            }

                            break;
                        case MapTypes.Battleground:
                        case MapTypes.Arena:
                            Logs.SQL.error("Battlegroundmap {0} specified to be disabled in map case, skipped.", entry);

                            continue;
                    }

                    if (isFlagInvalid) {
                        Logs.SQL.error("Disable flags for map {0} are invalid, skipped.", entry);

                        continue;
                    }

                    break;
                }
                case Battleground:
                    if (!CliDB.BattlemasterListStorage.containsKey(entry)) {
                        Logs.SQL.error("Battlegroundentry {0} from `disables` doesn't exist in dbc, skipped.", entry);

                        continue;
                    }

                    if (flags != 0) {
                        Logs.SQL.error("Disable flags specified for Battleground{0}, useless data.", entry);
                    }

                    break;
                case OutdoorPVP:
                    if (entry > OutdoorPvPTypes.max.getValue()) {
                        Logs.SQL.error("OutdoorPvPTypes value {0} from `disables` is invalid, skipped.", entry);

                        continue;
                    }

                    if (flags != 0) {
                        Logs.SQL.error("Disable flags specified for outdoor PvP {0}, useless data.", entry);
                    }

                    break;
                case Criteria:
                    if (global.getCriteriaMgr().getCriteria(entry) == null) {
                        Logs.SQL.error("Criteria entry {0} from `disables` doesn't exist in dbc, skipped.", entry);

                        continue;
                    }

                    if (flags != 0) {
                        Logs.SQL.error("Disable flags specified for Criteria {0}, useless data.", entry);
                    }

                    break;
                case VMAP: {
                    var mapEntry = CliDB.MapStorage.get(entry);

                    if (mapEntry == null) {
                        Logs.SQL.error("Map entry {0} from `disables` doesn't exist in dbc, skipped.", entry);

                        continue;
                    }

                    switch (mapEntry.instanceType) {
                        case MapTypes.Common:
                            if (flags.hasFlag(DisableFlags.VMAPAREAFLAG)) {
                                Log.outInfo(LogFilter.Server, "Areaflag disabled for world map {0}.", entry);
                            }

                            if (flags.hasFlag(DisableFlags.VMAPLIQUIDSTATUS)) {
                                Log.outInfo(LogFilter.Server, "Liquid status disabled for world map {0}.", entry);
                            }

                            break;
                        case MapTypes.Instance:
                        case MapTypes.Raid:
                            if (flags.hasFlag(DisableFlags.VMAPHEIGHT)) {
                                Log.outInfo(LogFilter.Server, "Height disabled for instance map {0}.", entry);
                            }

                            if (flags.hasFlag(DisableFlags.VMAPLOS)) {
                                Log.outInfo(LogFilter.Server, "LoS disabled for instance map {0}.", entry);
                            }

                            break;
                        case MapTypes.Battleground:
                            if (flags.hasFlag(DisableFlags.VMAPHEIGHT)) {
                                Log.outInfo(LogFilter.Server, "Height disabled for Battlegroundmap {0}.", entry);
                            }

                            if (flags.hasFlag(DisableFlags.VMAPLOS)) {
                                Log.outInfo(LogFilter.Server, "LoS disabled for Battlegroundmap {0}.", entry);
                            }

                            break;
                        case MapTypes.Arena:
                            if (flags.hasFlag(DisableFlags.VMAPHEIGHT)) {
                                Log.outInfo(LogFilter.Server, "Height disabled for arena map {0}.", entry);
                            }

                            if (flags.hasFlag(DisableFlags.VMAPLOS)) {
                                Log.outInfo(LogFilter.Server, "LoS disabled for arena map {0}.", entry);
                            }

                            break;
                        default:
                            break;
                    }

                    break;
                }
                case MMAP: {
                    var mapEntry = CliDB.MapStorage.get(entry);

                    if (mapEntry == null) {
                        Logs.SQL.error("Map entry {0} from `disables` doesn't exist in dbc, skipped.", entry);

                        continue;
                    }

                    switch (mapEntry.instanceType) {
                        case MapTypes.Common:
                            Log.outInfo(LogFilter.Server, "Pathfinding disabled for world map {0}.", entry);

                            break;
                        case MapTypes.Instance:
                        case MapTypes.Raid:
                            Log.outInfo(LogFilter.Server, "Pathfinding disabled for instance map {0}.", entry);

                            break;
                        case MapTypes.Battleground:
                            Log.outInfo(LogFilter.Server, "Pathfinding disabled for Battlegroundmap {0}.", entry);

                            break;
                        case MapTypes.Arena:
                            Log.outInfo(LogFilter.Server, "Pathfinding disabled for arena map {0}.", entry);

                            break;
                        default:
                            break;
                    }

                    break;
                }
                default:
                    break;
            }

            if (!m_DisableMap.containsKey(type)) {
                m_DisableMap.put(type, new HashMap<Integer, DisableData>());
            }

            m_DisableMap.get(type).put(entry, data);
            ++total_count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} disables in {1} ms", total_count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public final void checkQuestDisables() {
        if (!m_DisableMap.containsKey(DisableType.Quest) || m_DisableMap.get(DisableType.Quest).isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Checked 0 quest disables.");

            return;
        }

        var oldMSTime = System.currentTimeMillis();

        // check only quests, rest already done at startup
        for (var pair : m_DisableMap.get(DisableType.Quest)) {
            var entry = pair.key;

            if (global.getObjectMgr().getQuestTemplate(entry) == null) {
                Logs.SQL.error("Quest entry {0} from `disables` doesn't exist, skipped.", entry);
                m_DisableMap.get(DisableType.Quest).remove(entry);

                continue;
            }

            if (pair.value.flags != 0) {
                Logs.SQL.error("Disable flags specified for quest {0}, useless data.", entry);
            }
        }

        Log.outInfo(LogFilter.ServerLoading, "Checked {0} quest disables in {1} ms", m_DisableMap.get(DisableType.Quest).size(), time.GetMSTimeDiffToNow(oldMSTime));
    }


    public final boolean isDisabledFor(DisableType type, int entry, WorldObject refe) {
        return isDisabledFor(type, entry, refe, 0);
    }

    public final boolean isDisabledFor(DisableType type, int entry, WorldObject refe, short flags) {
        if (!m_DisableMap.containsKey(type) || m_DisableMap.get(type).isEmpty()) {
            return false;
        }

        var data = m_DisableMap.get(type).get(entry);

        if (data == null) // not disabled
        {
            return false;
        }

        switch (type) {
            case Spell: {
                var spellFlags = DisableFlags.forValue(data.flags);

                if (refe != null) {
                    if ((refe.isPlayer() && spellFlags.hasFlag(DisableFlags.SPELLPLAYER)) || (refe.isCreature() && (spellFlags.hasFlag(DisableFlags.SPELLCREATURE) || (refe.toUnit().isPet() && spellFlags.hasFlag(DisableFlags.SPELLPET)))) || (refe.isGameObject() && spellFlags.hasFlag(DisableFlags.SPELLGAMEOBJECT))) {
                        if (spellFlags.hasFlag(DisableFlags.SPELLARENAS.getValue() | DisableFlags.SPELLBATTLEGROUNDS.getValue())) {
                            var map = refe.getMap();

                            if (map != null) {
                                if (spellFlags.hasFlag(DisableFlags.SPELLARENAS) && map.isBattleArena()) {
                                    return true; // Current map is Arena and this spell is disabled here
                                }

                                if (spellFlags.hasFlag(DisableFlags.SPELLBATTLEGROUNDS) && map.isBattleground()) {
                                    return true; // Current map is a Battleground and this spell is disabled here
                                }
                            }
                        }

                        if (spellFlags.hasFlag(DisableFlags.SPELLMAP)) {
                            var mapIds = data.param0;

                            if (mapIds.contains(refe.getLocation().getMapId())) {
                                return true; // Spell is disabled on current map
                            }

                            if (!spellFlags.hasFlag(DisableFlags.SPELLAREA)) {
                                return false; // Spell is disabled on another map, but not this one, return false
                            }

                            // Spell is disabled in an area, but not explicitly our current mapId. Continue processing.
                        }

                        if (spellFlags.hasFlag(DisableFlags.SPELLAREA)) {
                            var areaIds = data.param1;

                            if (areaIds.contains(refe.getArea())) {
                                return true; // Spell is disabled in this area
                            }

                            return false; // Spell is disabled in another area, but not this one, return false
                        } else {
                            return true; // Spell disabled for all maps
                        }
                    }

                    return false;
                } else if (spellFlags.hasFlag(DisableFlags.SPELLDEPRECATEDSPELL)) // call not from spellcast
                {
                    return true;
                } else if (flags.hasFlag((byte) DisableFlags.SPELLLOS.getValue())) {
                    return spellFlags.hasFlag(DisableFlags.SPELLLOS);
                }

                break;
            }
            case Map:
            case LFGMap:
                var player = refe.toPlayer();

                if (player != null) {
                    var mapEntry = CliDB.MapStorage.get(entry);

                    if (mapEntry.IsDungeon()) {
                        var disabledModes = DisableFlags.forValue(data.flags);
                        var targetDifficulty = player.getDifficultyId(mapEntry);
                        tangible.RefObject<Difficulty> tempRef_targetDifficulty = new tangible.RefObject<Difficulty>(targetDifficulty);
                        global.getDB2Mgr().GetDownscaledMapDifficultyData(entry, tempRef_targetDifficulty);
                        targetDifficulty = tempRef_targetDifficulty.refArgValue;

                        switch (targetDifficulty) {
                            case Normal:
                                return disabledModes.hasFlag(DisableFlags.DUNGEONSTATUSNORMAL);
                            case Heroic:
                                return disabledModes.hasFlag(DisableFlags.DUNGEONSTATUSHEROIC);
                            case Raid10HC:
                                return disabledModes.hasFlag(DisableFlags.DUNGEONSTATUSHEROIC10MAN);
                            case Raid25HC:
                                return disabledModes.hasFlag(DisableFlags.DUNGEONSTATUSHEROIC25MAN);
                            default:
                                return false;
                        }
                    } else if (mapEntry.instanceType == MapTypes.Common) {
                        return true;
                    }
                }

                return false;
            case Quest:
                return true;
            case Battleground:
            case OutdoorPVP:
            case Criteria:
            case DisableType.MMAP:
                return true;
            case DisableType.VMAP:
                return flags.hasFlag(data.flags);
        }

        return false;
    }

    public final boolean isVMAPDisabledFor(int entry, byte flags) {
        return isDisabledFor(DisableType.VMAP, entry, null, flags);
    }

    public final boolean isPathfindingEnabled(int mapId) {
        return WorldConfig.getBoolValue(WorldCfg.EnableMmaps) && !global.getDisableMgr().isDisabledFor(DisableType.MMAP, mapId, null);
    }

    public static class DisableData {
        public short flags;
        public ArrayList<Integer> param0 = new ArrayList<>();
        public ArrayList<Integer> param1 = new ArrayList<>();
    }
}
