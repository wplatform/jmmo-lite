package com.github.azeroth.game.battlefield;


import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.map.Map;
import com.github.azeroth.game.listener.interfaces.ibattlefield.IBattlefieldGetBattlefield;

import java.util.HashMap;


public class BattleFieldManager {
    private static final int[] BATTLEFIELDIDTOMAPID = {0, 571, 732};

    private static final int[] BATTLEFIELDIDTOZONEID = {0, 4197, 5095}; // imitate World_PVP_Area.db2

    private static final int[] BATTLEFIELDIDTOSCRIPTID = {0, 0, 0};

    // contains all initiated battlefield events
    // used when initing / cleaning up
    private final MultiMap<Map, BattleField> battlefieldsByMap = new MultiMap<Map, BattleField>();

    // maps the zone ids to an battlefield event
    // used in player event handling
	private final HashMap<(
        private final limitedThreadTaskManager threadTaskManager = new limitedThreadTaskManager(ConfigMgr.GetDefaultValue("Map.ParellelUpdateTasks", 20));, Map map),BattleField>battlefieldsByZone =new HashMap<(
    int zoneId, Map map),BattleField>();
int zoneId
    // update interval
    private int updateTimer;

    private BattleFieldManager() {
    }

    public final void initBattlefield() {
        var oldMSTime = System.currentTimeMillis();

        int count = 0;
        var result = DB.World.query("SELECT TypeId, ScriptName FROM battlefield_template");

        if (!result.isEmpty()) {
            do {
                var typeId = BattleFieldTypes.forValue(result.<Byte>Read(0));

                if (typeId.getValue() >= BattleFieldTypes.max.getValue()) {
                    Logs.SQL.error(String.format("BattlefieldMgr::InitBattlefield: Invalid TypeId value %1$s in battlefield_template, skipped.", typeId));

                    continue;
                }

                BattlefieldIdToScriptId[typeId.getValue()] = global.getObjectMgr().getScriptId(result.<String>Read(1));
                ++count;
            } while (result.NextRow());
        }

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s battlefields in %2$s ms", count, time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public final void createBattlefieldsForMap(Map map) {
        for (int i = 0; i < BattleFieldTypes.max.getValue(); ++i) {
            if (BattlefieldIdToScriptId[i] == 0) {
                continue;
            }

            if (BattlefieldIdToMapId[i] != map.getId()) {
                continue;
            }

            var bf = global.getScriptMgr().<IBattlefieldGetBattlefield, BattleField>RunScriptRet(p -> p.GetBattlefield(map), BattlefieldIdToScriptId[i], null);

            if (bf == null) {
                continue;
            }

            if (!bf.setupBattlefield()) {
                Log.outInfo(LogFilter.Battlefield, String.format("Setting up battlefield with TypeId %1$s on map %2$s instance id %3$s failed.", BattleFieldTypes.forValue(i), map.getId(), map.getInstanceId()));

                continue;
            }

            battlefieldsByMap.add(map, bf);
            Log.outInfo(LogFilter.Battlefield, String.format("Setting up battlefield with TypeId %1$s on map %2$s instance id %3$s succeeded.", BattleFieldTypes.forValue(i), map.getId(), map.getInstanceId()));
        }
    }

    public final void destroyBattlefieldsForMap(Map map) {
        battlefieldsByMap.remove(map);
    }

    public final void addZone(int zoneId, BattleField bf) {
        battlefieldsByZone.put((bf.getMap(), zoneId), bf);
    }

    public final void handlePlayerEnterZone(Player player, int zoneId) {
        var bf = battlefieldsByZone.get((player.getMap(), zoneId));

        if (bf == null) {
            return;
        }

        if (!bf.isEnabled() || bf.hasPlayer(player)) {
            return;
        }

        bf.handlePlayerEnterZone(player, zoneId);
        Log.outDebug(LogFilter.Battlefield, "Player {0} entered battlefield id {1}", player.getGUID().toString(), bf.getTypeId());
    }

    public final void handlePlayerLeaveZone(Player player, int zoneId) {
        var bf = battlefieldsByZone.get((player.getMap(), zoneId));

        if (bf == null) {
            return;
        }

        // teleport: remove once in removefromworld, once in updatezone
        if (!bf.hasPlayer(player)) {
            return;
        }

        bf.handlePlayerLeaveZone(player, zoneId);
        Log.outDebug(LogFilter.Battlefield, "Player {0} left battlefield id {1}", player.getGUID().toString(), bf.getTypeId());
    }

    public final boolean isWorldPvpArea(int zoneId) {
        return BATTLEFIELDIDTOZONEID.contains(zoneId);
    }

    public final BattleField getBattlefieldToZoneId(Map map, int zoneId) {
        var bf = battlefieldsByZone.get((map, zoneId));

        if (bf == null) {
            // no handle for this zone, return
            return null;
        }

        if (!bf.isEnabled()) {
            return null;
        }

        return bf;
    }

    public final BattleField getBattlefieldByBattleId(Map map, int battleId) {
        var battlefields = battlefieldsByMap.get(map);

        for (var battlefield : battlefields) {
            if (battlefield.getBattleId() == battleId) {
                return battlefield;
            }
        }

        return null;
    }

    public final void update(int diff) {
        updateTimer += diff;

        if (updateTimer > 1000) {

            for (var(map, battlefield) : battlefieldsByMap.KeyValueList) {
                if (battlefield.isEnabled()) {
                    threadTaskManager.Schedule(() -> battlefield.update(updateTimer));
                }
            }

            threadTaskManager.Wait();
            updateTimer = 0;
        }
    }
}
