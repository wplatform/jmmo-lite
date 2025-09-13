package com.github.azeroth.game.scenario;


import com.github.azeroth.game.achievement.CriteriaManager;
import com.github.azeroth.game.map.InstanceMap;

import java.util.ArrayList;
import java.util.HashMap;


public class ScenarioManager {
    private final HashMap<Integer, ScenarioData> scenarioData = new HashMap<Integer, ScenarioData>();
    private final MultiMap<Integer, ScenarioPOI> scenarioPOIStore = new MultiMap<Integer, ScenarioPOI>();
    private final HashMap<Tuple<Integer, Byte>, ScenarioDBData> scenarioDBData = new HashMap<Tuple<Integer, Byte>, ScenarioDBData>();

    private ScenarioManager() {
    }

    public final InstanceScenario createInstanceScenario(InstanceMap map, int team) {
        var dbData = scenarioDBData.get(Tuple.create(map.getId(), (byte) map.getDifficultyID().getValue()));

        // No scenario registered for this map and difficulty in the database
        if (dbData == null) {
            return null;
        }

        int scenarioID = 0;

        switch (team) {
            case TeamId.ALLIANCE:
                scenarioID = dbData.scenario_A;

                break;
            case TeamId.HORDE:
                scenarioID = dbData.scenario_H;

                break;
            default:
                break;
        }

        var scenarioData = scenarioData.get(scenarioID);

        if (scenarioData == null) {
            Log.outError(LogFilter.Scenario, "Table `scenarios` contained data linking scenario (Id: {0}) to map (Id: {1}), difficulty (Id: {2}) but no scenario data was found related to that scenario id.", scenarioID, map.getId(), map.getDifficultyID());

            return null;
        }

        return new InstanceScenario(map, scenarioData);
    }

    public final void loadDBData() {
        scenarioDBData.clear();

        var oldMSTime = System.currentTimeMillis();

        var result = DB.World.query("SELECT map, difficulty, scenario_A, scenario_H FROM scenarios");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 scenarios. DB table `scenarios` is empty!");

            return;
        }

        do {
            var mapId = result.<Integer>Read(0);
            var difficulty = result.<Byte>Read(1);

            var scenarioAllianceId = result.<Integer>Read(2);

            if (scenarioAllianceId > 0 && !scenarioData.containsKey(scenarioAllianceId)) {
                Logs.SQL.error("ScenarioMgr.LoadDBData: DB Table `scenarios`, column scenario_A contained an invalid scenario (Id: {0})!", scenarioAllianceId);

                continue;
            }

            var scenarioHordeId = result.<Integer>Read(3);

            if (scenarioHordeId > 0 && !scenarioData.containsKey(scenarioHordeId)) {
                Logs.SQL.error("ScenarioMgr.LoadDBData: DB Table `scenarios`, column scenario_H contained an invalid scenario (Id: {0})!", scenarioHordeId);

                continue;
            }

            if (scenarioHordeId == 0) {
                scenarioHordeId = scenarioAllianceId;
            }

            ScenarioDBData data = new ScenarioDBData();
            data.mapID = mapId;
            data.difficultyID = difficulty;
            data.scenario_A = scenarioAllianceId;
            data.scenario_H = scenarioHordeId;
            scenarioDBData.put(Tuple.create(mapId, difficulty), data);
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} instance scenario entries in {1} ms", scenarioDBData.size(), time.GetMSTimeDiffToNow(oldMSTime));
    }

    public final void loadDB2Data() {
        scenarioData.clear();

        HashMap<Integer, HashMap<Byte, ScenarioStepRecord>> scenarioSteps = new HashMap<Integer, HashMap<Byte, ScenarioStepRecord>>();
        int deepestCriteriaTreeSize = 0;

        for (var step : CliDB.ScenarioStepStorage.values()) {
            if (!scenarioSteps.containsKey(step.scenarioID)) {
                scenarioSteps.put(step.scenarioID, new HashMap<Byte, ScenarioStepRecord>());
            }

            scenarioSteps.get(step.scenarioID).put(step.orderIndex, step);
            var tree = global.getCriteriaMgr().getCriteriaTree(step.CriteriaTreeId);

            if (tree != null) {
                int criteriaTreeSize = 0;
                CriteriaManager.walkCriteriaTree(tree, treeFunc ->
                {
                    ++criteriaTreeSize;
                });
                deepestCriteriaTreeSize = Math.max(deepestCriteriaTreeSize, criteriaTreeSize);
            }
        }

        //ASSERT(deepestCriteriaTreeSize < MAX_ALLOWED_SCENARIO_POI_QUERY_SIZE, "MAX_ALLOWED_SCENARIO_POI_QUERY_SIZE must be at least {0}", deepestCriteriaTreeSize + 1);

        for (var scenario : CliDB.ScenarioStorage.values()) {
            ScenarioData data = new ScenarioData();
            data.entry = scenario;
            data.steps = scenarioSteps.get(scenario.id);
            scenarioData.put(scenario.id, data);
        }
    }

    public final void loadScenarioPOI() {
        var oldMSTime = System.currentTimeMillis();

        scenarioPOIStore.clear(); // need for reload case

        int count = 0;

        //                                         0               1          2     3      4        5         6      7              8                  9
        var result = DB.World.query("SELECT criteriaTreeID, blobIndex, idx1, mapID, uiMapID, priority, flags, worldEffectID, playerConditionID, NavigationPlayerConditionID FROM scenario_poi ORDER BY criteriaTreeID, Idx1");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 scenario POI definitions. DB table `scenario_poi` is empty.");

            return;
        }

        HashMap<Integer, MultiMap<Integer, ScenarioPOIPoint>> allPoints = new HashMap<Integer, MultiMap<Integer, ScenarioPOIPoint>>();

        //                                               0               1    2  3  4
        var pointsResult = DB.World.query("SELECT criteriaTreeID, idx1, X, Y, Z FROM scenario_poi_points ORDER BY CriteriaTreeID DESC, idx1, Idx2");

        if (!pointsResult.isEmpty()) {
            do {
                var criteriaTreeID = pointsResult.<Integer>Read(0);
                var idx1 = pointsResult.<Integer>Read(1);
                var X = pointsResult.<Integer>Read(2);
                var Y = pointsResult.<Integer>Read(3);
                var Z = pointsResult.<Integer>Read(4);

                if (!allPoints.containsKey(criteriaTreeID)) {
                    allPoints.put(criteriaTreeID, new MultiMap<Integer, ScenarioPOIPoint>());
                }

                allPoints.get(criteriaTreeID).add(idx1, new ScenarioPOIPoint(X, Y, Z));
            } while (pointsResult.NextRow());
        }

        do {
            var criteriaTreeID = result.<Integer>Read(0);
            var blobIndex = result.<Integer>Read(1);
            var idx1 = result.<Integer>Read(2);
            var mapID = result.<Integer>Read(3);
            var uiMapID = result.<Integer>Read(4);
            var priority = result.<Integer>Read(5);
            var flags = result.<Integer>Read(6);
            var worldEffectID = result.<Integer>Read(7);
            var playerConditionID = result.<Integer>Read(8);
            var navigationPlayerConditionID = result.<Integer>Read(9);

            if (global.getCriteriaMgr().getCriteriaTree(criteriaTreeID) == null) {
                Logs.SQL.error(String.format("`scenario_poi` criteriaTreeID (%1$s) idx1 (%2$s) does not correspond to a valid criteria tree", criteriaTreeID, idx1));
            }

            var blobs = allPoints.get(criteriaTreeID);

            if (blobs != null) {
                var points = blobs.get(idx1);

                if (!points.isEmpty()) {
                    scenarioPOIStore.add(criteriaTreeID, new ScenarioPOI(blobIndex, mapID, uiMapID, priority, flags, worldEffectID, playerConditionID, navigationPlayerConditionID, points));
                    ++count;

                    continue;
                }
            }

            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                DB.World.execute(String.format("DELETE FROM scenario_poi WHERE criteriaTreeID = %1$s", criteriaTreeID));
            } else {
                Logs.SQL.error(String.format("Table scenario_poi references unknown scenario poi points for criteria tree id %1$s POI id %2$s", criteriaTreeID, blobIndex));
            }
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s scenario POI definitions in %2$s ms", count, time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public final ArrayList<ScenarioPOI> getScenarioPOIs(int criteriaTreeID) {
        if (!scenarioPOIStore.ContainsKey(criteriaTreeID)) {
            return null;
        }

        return scenarioPOIStore.get(criteriaTreeID);
    }
}
