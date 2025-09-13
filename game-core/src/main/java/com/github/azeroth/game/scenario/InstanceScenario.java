package com.github.azeroth.game.scenario;


import com.github.azeroth.game.achievement.CriteriaTree;
import com.github.azeroth.game.map.InstanceMap;
import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;
import java.util.HashMap;


public class InstanceScenario extends Scenario {
    private final InstanceMap map;

    public InstanceScenario(InstanceMap map, ScenarioData scenarioData) {
        super(scenarioData);
        map = map;

        //ASSERT(map);
        loadInstanceData();

        var players = map.getPlayers();

        for (var player : players) {
            sendScenarioState(player);
        }
    }

    @Override
    public String getOwnerInfo() {
        return String.format("Instance ID %1$s", map.getInstanceId());
    }

    @Override
    public void sendPacket(ServerPacket data) {
        //Hack  todo fix me
        if (map == null) {
            return;
        }

        map.sendToPlayers(data);
    }

    private void loadInstanceData() {
        var instanceScript = map.getInstanceScript();

        if (instanceScript == null) {
            return;
        }

        ArrayList<CriteriaTree> criteriaTrees = new ArrayList<>();

        var killCreatureCriteria = global.getCriteriaMgr().getScenarioCriteriaByTypeAndScenario(CriteriaType.KillCreature, DATA.entry.id);

        if (!killCreatureCriteria.isEmpty()) {
            var spawnGroups = global.getObjectMgr().getInstanceSpawnGroupsForMap(map.getId());

            if (spawnGroups != null) {
                HashMap<Integer, Long> despawnedCreatureCountsById = new HashMap<Integer, Long>();

                for (var spawnGroup : spawnGroups) {
                    if (instanceScript.getBossState(spawnGroup.bossStateId) != EncounterState.Done) {
                        continue;
                    }

                    var isDespawned = ((1 << EncounterState.Done.getValue()) & spawnGroup.bossStates) == 0 || spawnGroup.flags.hasFlag(InstanceSpawnGroupFlags.BlockSpawn);

                    if (isDespawned) {
                        for (var spawn : global.getObjectMgr().getSpawnMetadataForGroup(spawnGroup.spawnGroupId)) {
                            var spawnData = spawn.toSpawnData();

                            if (spawnData != null) {
                                ++despawnedCreatureCountsById.get(spawnData.id);
                            }
                        }
                    }
                }

                for (var criteria : killCreatureCriteria) {
                    // count creatures in despawned spawn groups
                    var progress = despawnedCreatureCountsById.get(criteria.entry.asset);

                    if (progress != 0) {
                        setCriteriaProgress(criteria, progress, null, ProgressType.set);
                        var trees = global.getCriteriaMgr().getCriteriaTreesByCriteria(criteria.id);

                        if (trees != null) {
                            for (var tree : trees) {
                                criteriaTrees.add(tree);
                            }
                        }
                    }
                }
            }
        }

        for (var criteria : global.getCriteriaMgr().getScenarioCriteriaByTypeAndScenario(CriteriaType.DefeatDungeonEncounter, DATA.entry.id)) {
            if (!instanceScript.isEncounterCompleted(criteria.entry.asset)) {
                continue;
            }

            setCriteriaProgress(criteria, 1, null, ProgressType.set);
            var trees = global.getCriteriaMgr().getCriteriaTreesByCriteria(criteria.id);

            if (trees != null) {
                for (var tree : trees) {
                    criteriaTrees.add(tree);
                }
            }
        }

        for (var tree : criteriaTrees) {
            var step = tree.scenarioStep;

            if (step == null) {
                continue;
            }


            if (isCompletedCriteriaTree(tree)) {
                setStepState(step, ScenarioStepState.Done);
            }
        }
    }
}
