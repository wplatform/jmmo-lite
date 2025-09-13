package com.github.azeroth.game.world;


import com.badlogic.gdx.utils.IntIntMap;
import com.github.azeroth.game.map.Map;
import com.github.azeroth.game.listener.interfaces.iworldstate.IWorldStateOnValueChange;

import java.util.HashMap;


public class WorldStateManager {
    private static final int ANYMAP = -1;
    private final HashMap<Integer, WorldStateTemplate> worldStateTemplates = new HashMap<Integer, WorldStateTemplate>();
    private final HashMap<Integer, Integer> realmWorldStateValues = new HashMap<Integer, Integer>();
    private final HashMap<Integer, HashMap<Integer, Integer>> worldStatesByMap = new HashMap<Integer, HashMap<Integer, Integer>>();

    private WorldStateManager() {
    }

    public void loadFromDB() {
        var oldMSTime = System.currentTimeMillis();

        //                                         0   1             2       3        4
        var result = DB.World.query("SELECT ID, defaultValue, MapIDs, AreaIDs, ScriptName FROM world_state");

        if (result.isEmpty()) {
            return;
        }

        do {
            var id = result.<Integer>Read(0);
            WorldStateTemplate worldState = new WorldStateTemplate();
            worldState.id = id;
            worldState.defaultValue = result.<Integer>Read(1);

            var mapIds = result.<String>Read(2);

            if (!mapIds.isEmpty()) {
                for (String mapIdToken : new LocalizedString(mapIds, ',')) {
                    int mapId;
                    tangible.OutObject<Integer> tempOut_mapId = new tangible.OutObject<Integer>();
                    if (!tangible.TryParseHelper.tryParseInt(mapIdToken, tempOut_mapId)) {
                        mapId = tempOut_mapId.outArgValue;
                        Logs.SQL.error(String.format("Table `world_state` contains a world state %1$s with non-integer mapID (%2$s), map ignored", id, mapIdToken));

                        continue;
                    } else {
                        mapId = tempOut_mapId.outArgValue;
                    }

                    if (mapId != ANYMAP && !CliDB.MapStorage.containsKey(mapId)) {
                        Logs.SQL.error(String.format("Table `world_state` contains a world state %1$s with invalid mapID (%2$s), map ignored", id, mapId));

                        continue;
                    }

                    worldState.mapIds.add(mapId);
                }
            }

            if (!mapIds.isEmpty() && worldState.mapIds.isEmpty()) {
                Logs.SQL.error(String.format("Table `world_state` contains a world state %1$s with nonempty MapIDs (%2$s) but no valid map id was found, ignored", id, mapIds));

                continue;
            }

            var areaIds = result.<String>Read(3);

            if (!areaIds.isEmpty() && !worldState.mapIds.isEmpty()) {
                for (String areaIdToken : new LocalizedString(areaIds, ',')) {
                    int areaId;
                    tangible.OutObject<Integer> tempOut_areaId = new tangible.OutObject<Integer>();
                    if (!tangible.TryParseHelper.tryParseInt(areaIdToken, tempOut_areaId)) {
                        areaId = tempOut_areaId.outArgValue;
                        Logs.SQL.error(String.format("Table `world_state` contains a world state %1$s with non-integer areaID (%2$s), area ignored", id, areaIdToken));

                        continue;
                    } else {
                        areaId = tempOut_areaId.outArgValue;
                    }

                    var areaTableEntry = CliDB.AreaTableStorage.get(areaId);

                    if (areaTableEntry == null) {
                        Logs.SQL.error(String.format("Table `world_state` contains a world state %1$s with invalid areaID (%2$s), area ignored", id, areaId));

                        continue;
                    }

                    if (!worldState.mapIds.contains(areaTableEntry.ContinentID)) {
                        Logs.SQL.error(String.format("Table `world_state` contains a world state %1$s with areaID (%2$s) not on any of required maps, area ignored", id, areaId));

                        continue;
                    }

                    worldState.areaIds.add(areaId);
                }

                if (!areaIds.isEmpty() && worldState.areaIds.isEmpty()) {
                    Logs.SQL.error(String.format("Table `world_state` contains a world state %1$s with nonempty AreaIDs (%2$s) but no valid area id was found, ignored", id, areaIds));

                    continue;
                }
            } else if (!areaIds.isEmpty()) {
                Logs.SQL.error(String.format("Table `world_state` contains a world state %1$s with nonempty AreaIDs (%2$s) but is a realm wide world state, area requirement ignored", id, areaIds));
            }

            worldState.scriptId = global.getObjectMgr().getScriptId(result.<String>Read(4));

            if (!worldState.mapIds.isEmpty()) {
                for (var mapId : worldState.mapIds) {
                    if (!worldStatesByMap.containsKey(mapId)) {
                        worldStatesByMap.put(mapId, new HashMap<Integer, Integer>());
                    }

                    worldStatesByMap.get(mapId).put(id, worldState.defaultValue);
                }
            } else {
                realmWorldStateValues.put(id, worldState.defaultValue);
            }

            worldStateTemplates.put(id, worldState);
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s world state templates %2$s ms", worldStateTemplates.size(), time.GetMSTimeDiffToNow(oldMSTime)));

        oldMSTime = System.currentTimeMillis();

        result = DB.characters.query("SELECT id, Value FROM world_state_value");
        int savedValueCount = 0;

        if (!result.isEmpty()) {
            do {
                var worldStateId = result.<Integer>Read(0);
                var worldState = worldStateTemplates.get(worldStateId);

                if (worldState == null) {
                    Logs.SQL.error(String.format("Table `world_state_value` contains a value for unknown world state %1$s, ignored", worldStateId));

                    continue;
                }

                var value = result.<Integer>Read(1);

                if (!worldState.mapIds.isEmpty()) {
                    for (var mapId : worldState.mapIds) {
                        if (!worldStatesByMap.containsKey(mapId)) {
                            worldStatesByMap.put(mapId, new HashMap<Integer, Integer>());
                        }

                        worldStatesByMap.get(mapId).put(worldStateId, value);
                    }
                } else {
                    realmWorldStateValues.put(worldStateId, value);
                }

                ++savedValueCount;
            } while (result.NextRow());
        }

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s saved world state values %2$s ms", savedValueCount, time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public final WorldStateTemplate getWorldStateTemplate(int worldStateId) {
        return worldStateTemplates.get(worldStateId);
    }

    public final int getValue(WorldStates worldStateId, Map map) {
        return getValue(worldStateId.getValue(), map);
    }

    public final int getValue(int worldStateId, Map map) {
        var worldStateTemplate = getWorldStateTemplate(worldStateId);

        if (worldStateTemplate == null || worldStateTemplate.mapIds.isEmpty()) {
            return realmWorldStateValues.get(worldStateId);
        }

        if (map == null || (!worldStateTemplate.mapIds.contains((int) map.getId()) && !worldStateTemplate.mapIds.contains(ANYMAP))) {
            return 0;
        }

        return map.getWorldStateValue(worldStateId);
    }

    public final void setValue(WorldStates worldStateId, int value, boolean hidden, Map map) {
        setValue(worldStateId.getValue(), value, hidden, map);
    }

    public final void setValue(int worldStateId, int value, boolean hidden, Map map) {
        setValue((int) worldStateId, value, hidden, map);
    }

    public final void setValue(int worldStateId, int value, boolean hidden, Map map) {
        var worldStateTemplate = getWorldStateTemplate(worldStateId);

        if (worldStateTemplate == null || worldStateTemplate.mapIds.isEmpty()) {
            var oldValue = 0;

            if (!realmWorldStateValues.TryAdd(worldStateId, 0)) {
                oldValue = realmWorldStateValues.get(worldStateId);

                if (oldValue == value) {
                    return;
                }
            }

            realmWorldStateValues.put(worldStateId, value);

            if (worldStateTemplate != null) {
                global.getScriptMgr().<IWorldStateOnValueChange>RunScript(script -> script.OnValueChange(worldStateTemplate.id, oldValue, value, null), worldStateTemplate.scriptId);
            }

            // Broadcast update to all players on the server
            UpdateWorldState updateWorldState = new updateWorldState();
            updateWorldState.variableID = (int) worldStateId;
            updateWorldState.value = value;
            updateWorldState.hidden = hidden;
            global.getWorldMgr().sendGlobalMessage(updateWorldState);

            return;
        }

        if (map == null || (!worldStateTemplate.mapIds.contains((int) map.getId()) && !worldStateTemplate.mapIds.contains(ANYMAP))) {
            return;
        }

        map.setWorldStateValue(worldStateId, value, hidden);
    }

    public final void saveValueInDb(int worldStateId, int value) {
        if (getWorldStateTemplate(worldStateId) == null) {
            return;
        }

        var stmt = DB.characters.GetPreparedStatement(CharStatements.REP_WORLD_STATE);
        stmt.AddValue(0, worldStateId);
        stmt.AddValue(1, value);
        DB.characters.execute(stmt);
    }

    public final void setValueAndSaveInDb(WorldStates worldStateId, int value, boolean hidden, Map map) {
        setValueAndSaveInDb(worldStateId.getValue(), value, hidden, map);
    }

    public final void setValueAndSaveInDb(int worldStateId, int value, boolean hidden, Map map) {
        setValue(worldStateId, value, hidden, map);
        saveValueInDb(worldStateId, value);
    }

    public final IntIntMap getInitialWorldStatesForMap(Map map) {
        IntIntMap initialValues = new IntIntMap();

        TValue valuesTemplate;
        if (worldStatesByMap.containsKey((int) map.getId()) && (valuesTemplate = worldStatesByMap.get((int) map.getId())) == valuesTemplate) {

            for (var(key, value) : valuesTemplate) {
                initialValues.put(key, value);
            }
        }

        if (worldStatesByMap.containsKey(ANYMAP) && (valuesTemplate = worldStatesByMap.get(ANYMAP)) == valuesTemplate) {

            for (var(key, value) : valuesTemplate) {
                initialValues.put(key, value);
            }
        }

        return initialValues;
    }

    public final void fillInitialWorldStates(InitWorldStates initWorldStates, Map map, int playerAreaId) {

        for (var(worldStateId, value) : realmWorldStateValues) {
            initWorldStates.addState(worldStateId, value);
        }


        for (var(worldStateId, value) : map.getWorldStateValues()) {
            var worldStateTemplate = getWorldStateTemplate(worldStateId);

            if (worldStateTemplate != null && !worldStateTemplate.areaIds.isEmpty()) {
                var isInAllowedArea = worldStateTemplate.areaIds.Any(requiredAreaId -> global.getDB2Mgr().IsInArea(playerAreaId, requiredAreaId));

                if (!isInAllowedArea) {
                    continue;
                }
            }

            initWorldStates.addState(worldStateId, value);
        }
    }
}
