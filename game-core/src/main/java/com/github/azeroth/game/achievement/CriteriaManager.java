package com.github.azeroth.game.achievement;


import java.util.ArrayList;
import java.util.HashMap;


public class CriteriaManager {
    private final HashMap<Integer, CriteriaDataSet> criteriaDataMap = new HashMap<Integer, CriteriaDataSet>();
    private final HashMap<Integer, CriteriaTree> criteriaTrees = new HashMap<Integer, CriteriaTree>();
    private final HashMap<Integer, criteria> criteria = new HashMap<Integer, criteria>();
    private final HashMap<Integer, ModifierTreeNode> criteriaModifiers = new HashMap<Integer, ModifierTreeNode>();
    private final MultiMap<Integer, CriteriaTree> criteriaTreeByCriteria = new MultiMap<Integer, CriteriaTree>();

    // store criterias by type to speed up lookup
    private final MultiMap<CriteriaType, criteria> criteriasByType = new MultiMap<CriteriaType, criteria>();
    private final MultiMap<Integer, criteria>[] criteriasByAsset = new MultiMap<Integer, criteria>[CriteriaType.count.getValue()];
    private final MultiMap<CriteriaType, criteria> guildCriteriasByType = new MultiMap<CriteriaType, criteria>();
    private final MultiMap<Integer, criteria>[] scenarioCriteriasByTypeAndScenarioId = new MultiMap<Integer, criteria>[CriteriaType.count.getValue()];
    private final MultiMap<CriteriaType, criteria> questObjectiveCriteriasByType = new MultiMap<CriteriaType, criteria>();
    private final MultiMap<CriteriaStartEvent, criteria> criteriasByTimedType = new MultiMap<CriteriaStartEvent, criteria>();
    private final MultiMap<Integer, criteria>[] criteriasByFailEvent = new MultiMap<Integer, criteria>[CriteriaFailEvent.max.getValue()];

    private CriteriaManager() {
        for (var i = 0; i < CriteriaType.count.getValue(); ++i) {
            _criteriasByAsset[i] = new MultiMap<Integer, criteria>();
            _scenarioCriteriasByTypeAndScenarioId[i] = new MultiMap<Integer, criteria>();
        }
    }

    public static boolean isGroupCriteriaType(CriteriaType type) {
        switch (type) {
            case KillCreature:
            case WinBattleground:
            case BeSpellTarget: // NYI
            case WinAnyRankedArena:
            case GainAura: // NYI
            case WinAnyBattleground: // NYI
                return true;
            default:
                break;
        }

        return false;
    }

    public static void walkCriteriaTree(CriteriaTree tree, tangible.Action1Param<CriteriaTree> func) {
        for (var node : tree.children) {
            walkCriteriaTree(node, func);
        }

        func.invoke(tree);
    }

    public final void loadCriteriaModifiersTree() {
        var oldMSTime = System.currentTimeMillis();

        if (CliDB.ModifierTreeStorage.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 criteria modifiers.");

            return;
        }

        // Load modifier tree nodes
        for (var tree : CliDB.ModifierTreeStorage.values()) {
            ModifierTreeNode node = new ModifierTreeNode();
            node.entry = tree;
            criteriaModifiers.put(node.entry.id, node);
        }

        // Build tree
        for (var treeNode : criteriaModifiers.values()) {
            var parentNode = criteriaModifiers.get(treeNode.entry.parent);

            if (parentNode != null) {
                parentNode.children.add(treeNode);
            }
        }

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} criteria modifiers in {1} ms", criteriaModifiers.size(), time.GetMSTimeDiffToNow(oldMSTime));
    }

    public final void loadCriteriaList() {
        var oldMSTime = System.currentTimeMillis();

        HashMap<Integer, AchievementRecord> achievementCriteriaTreeIds = new HashMap<Integer, AchievementRecord>();

        for (var achievement : CliDB.AchievementStorage.values()) {
            if (achievement.CriteriaTree != 0) {
                achievementCriteriaTreeIds.put(achievement.CriteriaTree, achievement);
            }
        }

        HashMap<Integer, ScenarioStepRecord> scenarioCriteriaTreeIds = new HashMap<Integer, ScenarioStepRecord>();

        for (var scenarioStep : CliDB.ScenarioStepStorage.values()) {
            if (scenarioStep.CriteriaTreeId != 0) {
                scenarioCriteriaTreeIds.put(scenarioStep.CriteriaTreeId, scenarioStep);
            }
        }

        HashMap<Integer, questObjective> questObjectiveCriteriaTreeIds = new HashMap<Integer, questObjective>();

        for (var pair : global.getObjectMgr().getQuestTemplates().entrySet()) {
            for (var objective : pair.getValue().objectives) {
                if (objective.type != QuestObjectiveType.CriteriaTree) {
                    continue;
                }

                if (objective.objectID != 0) {
                    questObjectiveCriteriaTreeIds.put((int) objective.objectID, objective);
                }
            }
        }

        // Load criteria tree nodes
        for (var tree : CliDB.CriteriaTreeStorage.values()) {
            // Find linked achievement
            var achievement = getEntry(achievementCriteriaTreeIds, tree);
            var scenarioStep = getEntry(scenarioCriteriaTreeIds, tree);
            var questObjective = getEntry(questObjectiveCriteriaTreeIds, tree);

            if (achievement == null && scenarioStep == null && questObjective == null) {
                continue;
            }

            CriteriaTree criteriaTree = new CriteriaTree();
            criteriaTree.id = tree.id;
            criteriaTree.achievement = achievement;
            criteriaTree.scenarioStep = scenarioStep;
            criteriaTree.questObjective = questObjective;
            criteriaTree.entry = tree;

            criteriaTrees.put(criteriaTree.entry.id, criteriaTree);
        }

        // Build tree
        for (var pair : criteriaTrees.entrySet()) {
            var parent = criteriaTrees.get(pair.getValue().entry.parent);

            if (parent != null) {
                parent.children.add(pair.getValue());
            }

            if (CliDB.CriteriaStorage.HasRecord(pair.getValue().entry.criteriaID)) {
                criteriaTreeByCriteria.add(pair.getValue().entry.criteriaID, pair.getValue());
            }
        }

        for (var i = 0; i < CriteriaFailEvent.max.getValue(); ++i) {
            _criteriasByFailEvent[i] = new MultiMap<Integer, criteria>();
        }

        // Load criteria
        int criterias = 0;
        int guildCriterias = 0;
        int scenarioCriterias = 0;
        int questObjectiveCriterias = 0;

        for (var criteriaEntry : CliDB.CriteriaStorage.values()) {
            var treeList = criteriaTreeByCriteria.get(criteriaEntry.id);

            if (treeList.isEmpty()) {
                continue;
            }

            Criteria criteria = new criteria();
            criteria.id = criteriaEntry.id;
            criteria.entry = criteriaEntry;
            criteria.modifier = criteriaModifiers.get(criteriaEntry.ModifierTreeId);

            criteria.put(criteria.id, criteria);

            ArrayList<Integer> scenarioIds = new ArrayList<>();

            for (var tree : treeList) {
                tree.criteria = criteria;

                var achievement = tree.achievement;

                if (achievement != null) {
                    if (achievement.flags.hasFlag(AchievementFlags.guild)) {
                        criteria.flagsCu = CriteriaFlagsCu.forValue(criteria.flagsCu.getValue() | CriteriaFlagsCu.guild.getValue());
                    } else if (achievement.flags.hasFlag(AchievementFlags.Account)) {
                        criteria.flagsCu = CriteriaFlagsCu.forValue(criteria.flagsCu.getValue() | CriteriaFlagsCu.Account.getValue());
                    } else {
                        criteria.flagsCu = CriteriaFlagsCu.forValue(criteria.flagsCu.getValue() | CriteriaFlagsCu.player.getValue());
                    }
                } else if (tree.scenarioStep != null) {
                    criteria.flagsCu = CriteriaFlagsCu.forValue(criteria.flagsCu.getValue() | CriteriaFlagsCu.Scenario.getValue());
                    scenarioIds.add(tree.scenarioStep.scenarioID);
                } else if (tree.questObjective != null) {
                    criteria.flagsCu = CriteriaFlagsCu.forValue(criteria.flagsCu.getValue() | CriteriaFlagsCu.questObjective.getValue());
                }
            }

            if (criteria.flagsCu.hasFlag(CriteriaFlagsCu.player.getValue() | CriteriaFlagsCu.Account.getValue())) {
                ++criterias;
                criteriasByType.add(criteriaEntry.type, criteria);

                if (isCriteriaTypeStoredByAsset(criteriaEntry.type)) {
                    if (criteriaEntry.type != CriteriaType.RevealWorldMapOverlay) {
                        _criteriasByAsset[(int) criteriaEntry.Type].add(criteriaEntry.asset, criteria);
                    } else {
                        var worldOverlayEntry = CliDB.WorldMapOverlayStorage.get(criteriaEntry.asset);

                        if (worldOverlayEntry == null) {
                            break;
                        }

                        for (byte j = 0; j < SharedConst.MaxWorldMapOverlayArea; ++j) {
                            if (worldOverlayEntry.AreaID[j] != 0) {
                                var valid = true;

                                for (byte i = 0; i < j; ++i) {
                                    if (worldOverlayEntry.AreaID[j] == worldOverlayEntry.AreaID[i]) {
                                        valid = false;
                                    }
                                }

                                if (valid) {
                                    _criteriasByAsset[(int) criteriaEntry.Type].add(worldOverlayEntry.AreaID[j], criteria);
                                }
                            }
                        }
                    }
                }
            }

            if (criteria.flagsCu.hasFlag(CriteriaFlagsCu.guild)) {
                ++guildCriterias;
                guildCriteriasByType.add(criteriaEntry.type, criteria);
            }

            if (criteria.flagsCu.hasFlag(CriteriaFlagsCu.Scenario)) {
                ++scenarioCriterias;

                for (var scenarioId : scenarioIds) {
                    _scenarioCriteriasByTypeAndScenarioId[(int) criteriaEntry.Type].add(scenarioId, criteria);
                }
            }

            if (criteria.flagsCu.hasFlag(CriteriaFlagsCu.questObjective)) {
                ++questObjectiveCriterias;
                questObjectiveCriteriasByType.add(criteriaEntry.type, criteria);
            }

            if (criteriaEntry.startTimer != 0) {
                criteriasByTimedType.add(CriteriaStartEvent.forValue(criteriaEntry.StartEvent), criteria);
            }

            if (criteriaEntry.FailEvent != 0) {
                _criteriasByFailEvent[criteriaEntry.FailEvent].add((int) criteriaEntry.FailAsset, criteria);
            }
        }

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s criteria, %2$s guild criteria, %3$s scenario criteria and %4$s quest objective criteria in %5$s ms.", criterias, guildCriterias, scenarioCriterias, questObjectiveCriterias, time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public final void loadCriteriaData() {
        var oldMSTime = System.currentTimeMillis();

        criteriaDataMap.clear(); // need for reload case

        var result = DB.World.query("SELECT criteria_id, type, value1, value2, ScriptName FROM criteria_data");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 additional criteria data. DB table `criteria_data` is empty.");

            return;
        }

        int count = 0;

        do {
            var criteria_id = result.<Integer>Read(0);

            var criteria = getCriteria(criteria_id);

            if (criteria == null) {
                Logs.SQL.error("Table `criteria_data` contains data for non-existing criteria (Entry: {0}). Ignored.", criteria_id);

                continue;
            }

            var dataType = CriteriaDataType.forValue(result.<Byte>Read(1));
            var scriptName = result.<String>Read(4);
            int scriptId = 0;

            if (!scriptName.isEmpty()) {
                if (dataType != CriteriaDataType.script) {
                    Logs.SQL.error("Table `criteria_data` contains a ScriptName for non-scripted data type (Entry: {0}, type {1}), useless data.", criteria_id, dataType);
                } else {
                    scriptId = global.getObjectMgr().getScriptId(scriptName);
                }
            }

            CriteriaData data = new CriteriaData(dataType, result.<Integer>Read(2), result.<Integer>Read(3), scriptId);

            if (!data.isValid(criteria)) {
                continue;
            }

            // this will allocate empty data set storage
            CriteriaDataSet dataSet = new CriteriaDataSet();
            dataSet.setCriteriaId(criteria_id);

            // add real data only for not NONE data types
            if (data.dataType != CriteriaDataType.NONE) {
                dataSet.add(data);
            }

            criteriaDataMap.put(criteria_id, dataSet);
            // counting data by and data types
            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} additional criteria data in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public final CriteriaTree getCriteriaTree(int criteriaTreeId) {
        return criteriaTrees.get(criteriaTreeId);
    }

    public final Criteria getCriteria(int criteriaId) {
        return criteria.get(criteriaId);
    }

    public final ModifierTreeNode getModifierTree(int modifierTreeId) {
        return criteriaModifiers.get(modifierTreeId);
    }

    public final ArrayList<criteria> getPlayerCriteriaByType(CriteriaType type, int asset) {
        if (asset != 0 && isCriteriaTypeStoredByAsset(type)) {
            if (_criteriasByAsset[type.getValue()].ContainsKey(asset)) {
                return _criteriasByAsset[type.getValue()].get(asset);
            }

            return new ArrayList<>();
        }

        return criteriasByType.get(type);
    }

    public final ArrayList<criteria> getScenarioCriteriaByTypeAndScenario(CriteriaType type, int scenarioId) {
        return _scenarioCriteriasByTypeAndScenarioId[type.getValue()].get(scenarioId);
    }

    public final ArrayList<criteria> getGuildCriteriaByType(CriteriaType type) {
        return guildCriteriasByType.get(type);
    }

    public final ArrayList<criteria> getQuestObjectiveCriteriaByType(CriteriaType type) {
        return questObjectiveCriteriasByType.get(type);
    }

    public final ArrayList<CriteriaTree> getCriteriaTreesByCriteria(int criteriaId) {
        return criteriaTreeByCriteria.get(criteriaId);
    }

    public final ArrayList<criteria> getTimedCriteriaByType(CriteriaStartEvent startEvent) {
        return criteriasByTimedType.get(startEvent);
    }

    public final ArrayList<criteria> getCriteriaByFailEvent(CriteriaFailEvent failEvent, int asset) {
        return _criteriasByFailEvent[failEvent.getValue()].get(asset);
    }

    public final CriteriaDataSet getCriteriaDataSet(Criteria criteria) {
        return criteriaDataMap.get(criteria.id);
    }


    private <T> T getEntry(HashMap<Integer, T> map, CriteriaTreeRecord tree) {
        var cur = tree;
        var obj = map.get(tree.id);

        while (obj == null) {
            if (cur.parent == 0) {
                break;
            }

            cur = CliDB.CriteriaTreeStorage.get(cur.parent);

            if (cur == null) {
                break;
            }

            obj = map.get(cur.id);
        }

        if (obj == null) {
            return null;
        }

        return obj;
    }

    private boolean isCriteriaTypeStoredByAsset(CriteriaType type) {
        switch (type) {
            case KillCreature:
            case WinBattleground:
            case SkillRaised:
            case EarnAchievement:
            case CompleteQuestsInZone:
            case ParticipateInBattleground:
            case KilledByCreature:
            case CompleteQuest:
            case BeSpellTarget:
            case CastSpell:
            case TrackedWorldStateUIModified:
            case PVPKillInArea:
            case LearnOrKnowSpell:
            case AcquireItem:
            case AchieveSkillStep:
            case UseItem:
            case LootItem:
            case RevealWorldMapOverlay:
            case ReputationGained:
            case EquipItemInSlot:
            case DeliverKillingBlowToClass:
            case DeliverKillingBlowToRace:
            case DoEmote:
            case EquipItem:
            case UseGameobject:
            case GainAura:
            case CatchFishInFishingHole:
            case LearnSpellFromSkillLine:
            case GetLootByType:
            case LandTargetedSpellOnTarget:
            case LearnTradeskillSkillLine:
                return true;
            default:
                return false;
        }
    }
}
