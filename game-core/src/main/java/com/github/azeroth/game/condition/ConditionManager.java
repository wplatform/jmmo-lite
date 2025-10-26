package com.github.azeroth.game.condition;


import com.github.azeroth.common.Logs;
import com.github.azeroth.dbc.defines.PlayerConditionLfgStatus;
import com.github.azeroth.dbc.defines.UnitConditionVariable;
import com.github.azeroth.dbc.defines.WorldStateExpressionFunction;
import com.github.azeroth.dbc.domain.PlayerCondition;
import com.github.azeroth.dbc.domain.WorldStateExpression;
import com.github.azeroth.defines.*;
import com.github.azeroth.game.domain.condition.*;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.domain.object.enums.TypeId;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.player.PlayerDefine;
import com.github.azeroth.game.entity.player.enums.EquipmentSlot;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.loot.LootStorage;
import com.github.azeroth.game.loot.LootTemplate;
import com.github.azeroth.game.map.grid.GridMapTypeMask;
import com.github.azeroth.game.spell.AuraFlags;
import com.github.azeroth.game.spell.auras.enums.AuraType;
import com.github.azeroth.game.world.WorldContext;

import java.util.*;

public final class ConditionManager {
    public static EnumMap<ConditionSourceType, Map<ConditionId, List<Condition>>> conditionStore = new EnumMap<>(ConditionSourceType.class);
    Set<Integer> spellsUsedInSpellClickConditions = new HashSet<>();

    public ConditionTypeInfo[] staticConditionTypeData = {
            ConditionTypeInfo.of("None", false, false, false, false),
            ConditionTypeInfo.of("Aura", true, true, true, false),
            ConditionTypeInfo.of("Item Stored", true, true, true, false),
            ConditionTypeInfo.of("Item Equipped", true, false, false, false),
            ConditionTypeInfo.of("Zone", true, false, false, false),
            ConditionTypeInfo.of("Reputation", true, true, false, false),
            ConditionTypeInfo.of("Team", true, false, false, false),
            ConditionTypeInfo.of("Skill", true, true, false, false),
            ConditionTypeInfo.of("Quest Rewarded", true, false, false, false),
            ConditionTypeInfo.of("Quest Taken", true, false, false, false),
            ConditionTypeInfo.of("Drunken", true, false, false, false),
            ConditionTypeInfo.of("WorldState", true, true, false, false),
            ConditionTypeInfo.of("Active Event", true, false, false, false),
            ConditionTypeInfo.of("Instance Info", true, true, true, false),
            ConditionTypeInfo.of("Quest None", true, false, false, false),
            ConditionTypeInfo.of("Class", true, false, false, false),
            ConditionTypeInfo.of("Race", true, false, false, false),
            ConditionTypeInfo.of("Achievement", true, false, false, false),
            ConditionTypeInfo.of("Title", true, false, false, false),
            ConditionTypeInfo.of("SpawnMask", true, false, false, false),
            ConditionTypeInfo.of("Gender", true, false, false, false),
            ConditionTypeInfo.of("Unit State", true, false, false, false),
            ConditionTypeInfo.of("Map", true, false, false, false),
            ConditionTypeInfo.of("Area", true, false, false, false),
            ConditionTypeInfo.of("CreatureType", true, false, false, false),
            ConditionTypeInfo.of("Spell Known", true, false, false, false),
            ConditionTypeInfo.of("Phase", true, false, false, false),
            ConditionTypeInfo.of("Level", true, true, false, false),
            ConditionTypeInfo.of("Quest Completed", true, false, false, false),
            ConditionTypeInfo.of("Near Creature", true, true, true, false),
            ConditionTypeInfo.of("Near GameObject", true, true, false, false),
            ConditionTypeInfo.of("Object Entry or Guid", true, true, true, false),
            ConditionTypeInfo.of("Object TypeMask", true, false, false, false),
            ConditionTypeInfo.of("Relation", true, true, false, false),
            ConditionTypeInfo.of("Reaction", true, true, false, false),
            ConditionTypeInfo.of("Distance", true, true, true, false),
            ConditionTypeInfo.of("Alive", false, false, false, false),
            ConditionTypeInfo.of("Health Value", true, true, false, false),
            ConditionTypeInfo.of("Health Pct", true, true, false, false),
            ConditionTypeInfo.of("Realm Achievement", true, false, false, false),
            ConditionTypeInfo.of("In Water", false, false, false, false),
            ConditionTypeInfo.of("Terrain Swap", true, false, false, false),
            ConditionTypeInfo.of("Sit/stand state", true, true, false, false),
            ConditionTypeInfo.of("Daily Quest Completed", true, false, false, false),
            ConditionTypeInfo.of("Charmed", false, false, false, false),
            ConditionTypeInfo.of("Pet type", true, false, false, false),
            ConditionTypeInfo.of("On Taxi", false, false, false, false),
            ConditionTypeInfo.of("Quest state mask", true, true, false, false),
            ConditionTypeInfo.of("Quest objective progress", true, false, true, false),
            ConditionTypeInfo.of("Map Difficulty", true, false, false, false),
            ConditionTypeInfo.of("Is Gamemaster", true, false, false, false),
            ConditionTypeInfo.of("Object Entry or Guid", true, true, true, false),
            ConditionTypeInfo.of("Object TypeMask", true, false, false, false),
            ConditionTypeInfo.of("BattlePet Species Learned", true, true, true, false),
            ConditionTypeInfo.of("On Scenario Step", true, false, false, false),
            ConditionTypeInfo.of("Scene In Progress", true, false, false, false),
            ConditionTypeInfo.of("Player Condition", true, false, false, false),
            ConditionTypeInfo.of("Private Object", false, false, false, false),
            ConditionTypeInfo.of("String ID", false, false, false, true)
    };

    private WorldContext worldContext;

    private ConditionManager() {
    }

    public int getPlayerConditionLfgValue(Player player, PlayerConditionLfgStatus status) {
        if (player.getGroup() == null) {
            return 0;
        }
        switch (status) {
            case InLFGDungeon:
                return worldContext.getLfgManager().inLfgDungeonMap(player.getGUID(), player.getLocation().getMapId(), player.getMap().getDifficultyID()) ? 1 : 0;
            case InLFGRandomDungeon:
                return worldContext.getLfgManager().inLfgDungeonMap(player.getGUID(), player.getLocation().getMapId(), player.getMap().getDifficultyID()) && worldContext.getLfgManager().selectedRandomLfgDungeon(player.getGUID()) ? 1 : 0;
            case InLFGFirstRandomDungeon: {
                if (!worldContext.getLfgManager().inLfgDungeonMap(player.getGUID(), player.getLocation().getMapId(), player.getMap().getDifficultyID())) {
                    return 0;
                }
                var selectedRandomDungeon = worldContext.getLfgManager().getSelectedRandomDungeon(player.getGUID());
                if (selectedRandomDungeon == 0) {
                    return 0;
                }
                var reward = worldContext.getLfgManager().getRandomDungeonReward(selectedRandomDungeon, player.getLevel());
                if (reward != null) {
                    var quest = worldContext.getObjectManager().getQuestTemplate(reward.firstQuest);
                    if (quest != null) {
                        if (player.canRewardQuest(quest, false)) {
                            return 1;
                        }
                    }
                }
                return 0;
            }
            default:
                break;
        }
        return 0;
    }


    boolean isPlayerMeetingCondition(Player player, int conditionId) {
        if (conditionId == 0)
            return true;

        if (!isObjectMeetingNotGroupedConditions(ConditionSourceType.PLAYER_CONDITION, conditionId, player))
            return false;

        var playerCondition = worldContext.getDbcObjectManager().playerCondition(conditionId);
        if (playerCondition != null)
            return isPlayerMeetingCondition(player, playerCondition);

        return true;
    }

    public boolean isPlayerMeetingCondition(Player player, PlayerCondition condition) {

        if (!condition.getRaceMask().isEmpty() && !condition.getRaceMask().hasRace(player.getRace()))
            return false;

        if (condition.getClassMask() != 0 && (player.getClassMask() & condition.getClassMask()) == 0)
            return false;

        if (condition.getGender() >= 0 && player.getGender().value != condition.getGender())
            return false;

        if (condition.getNativeGender() >= 0 && player.getNativeGender().value != condition.getNativeGender())
            return false;

        if (condition.getPowerType() != -1 && condition.getPowerTypeComp() != 0) {
            int requiredPowerValue = (condition.getFlags() & 4) != 0 ? player.getMaxPower(Power.valueOf(condition.getPowerType())) : condition.getPowerTypeValue();
            if (!playerConditionCompare(condition.getPowerTypeComp(), player.getPower(Power.valueOf(condition.getPowerType())), requiredPowerValue))
                return false;
        }

        short[] skillID = condition.getSkillID();
        short[] minSkill = condition.getMinSkill();
        short[] maxSkill = condition.getMaxSkill();
        if (skillID[0] != 0 || skillID[1] != 0 || skillID[2] != 0 || skillID[3] != 0) {
            var results = new boolean[skillID.length];
            Arrays.fill(results, true);
            for (int i = 0; i < skillID.length; ++i) {
                if (skillID[i] != 0) {
                    short skillValue = player.getSkillValue(SkillType.valueOf(skillID[i]));
                    results[i] = skillValue != 0 && skillValue > minSkill[i] && skillValue < maxSkill[i];
                }
            }

            if (!playerConditionLogic(condition.getSkillLogic(), results))
                return false;
        }

        if (condition.getLanguageID() != 0) {
            int languageSkill = 0;
            if (player.hasAuraTypeWithMiscvalue(AuraType.COMPREHEND_LANGUAGE, condition.getLanguageID()))
                languageSkill = 300;
            else {
                var languageManager = worldContext.getLanguageManager();
                var languageDescById = languageManager.getLanguageDescById(Language.valueOf(condition.getLanguageID()));
                for (var languageDesc : languageDescById)
                    languageSkill = Math.max(languageSkill, player.getSkillValue(SkillType.valueOf(languageDesc.skillId)));
            }

            if (condition.getMinLanguage() != 0 && languageSkill < condition.getMinLanguage())
                return false;

            if (condition.getMaxLanguage() != 0 && languageSkill > condition.getMaxLanguage())
                return false;
        }

        int[] minFactionID = condition.getMinFactionID();
        if (minFactionID[0] != 0 || minFactionID[1] != 0 || minFactionID[2] != 0 || condition.getMaxFactionID() != 0) {
            if (minFactionID[0] == 0 && minFactionID[1] == 0 && minFactionID[2] == 0) {
                ReputationRank forcedRank = player.getReputationMgr().getForcedRankIfAny(condition.getMaxFactionID());
                if (forcedRank != null) {
                    ReputationRank maxReputation = ReputationRank.values()[condition.getMaxReputation()];
                    if (forcedRank.compareTo(maxReputation) > 0)
                        return false;
                } else if (worldContext.getDbcObjectManager().faction().contains(condition.getMaxReputation())
                        && player.getReputationRank(condition.getMaxFactionID()).compareTo(ReputationRank.values()[condition.getMaxReputation()]) > 0)
                    return false;
            } else {
                boolean[] results = new boolean[minFactionID.length + 1];
                Arrays.fill(results, true);
                for (int i = 0; i < minFactionID.length; ++i) {
                    if (worldContext.getDbcObjectManager().faction().contains(minFactionID[i])) {
                        ReputationRank forcedRank = player.getReputationMgr().getForcedRankIfAny(minFactionID[i]);
                        byte[] minReputation = condition.getMinReputation();
                        if (forcedRank != null)
                            results[i] = forcedRank.compareTo(ReputationRank.values()[minReputation[i]]) >= 0;
                        else
                            results[i] = player.getReputationRank(minFactionID[i]).compareTo(ReputationRank.values()[minReputation[i]]) >= 0;
                    }
                }

                ReputationRank forcedRank = player.getReputationMgr().getForcedRankIfAny(condition.getMaxFactionID());
                if (forcedRank != null) {
                    results[3] = forcedRank.compareTo(ReputationRank.values()[condition.getMaxReputation()]) <= 0;
                } else if (worldContext.getDbcObjectManager().faction().contains(condition.getMaxReputation()))
                    results[3] = player.getReputationRank(condition.getMaxFactionID()).compareTo(ReputationRank.values()[condition.getMaxReputation()]) <= 0;

                if (!playerConditionLogic(condition.getReputationLogic(), results))
                    return false;
            }
        }

        if (condition.getCurrentPvpFaction() != 0) {
            byte team;
            if (player.getMap().isBattlegroundOrArena())
                team = (byte) player.getPlayerData().getArenaFaction();
            else
                team = (byte) (player.getTeamId() == TeamId.ALLIANCE ? 1 : 0);

            if (condition.getCurrentPvpFaction() - 1 != team)
                return false;
        }

        if (condition.getPvpMedal() != 0 && ((1 << (condition.getPvpMedal() - 1)) & player.getActivePlayerData().getPvpMedals()) == 0)
            return false;

        if (condition.getLifetimeMaxPVPRank() != 0 && player.getActivePlayerData().getLifetimeMaxRank() != condition.getLifetimeMaxPVPRank())
            return false;

        if (condition.getMovementFlags1() != 0 && (player.getUnitMovementFlags() & condition.getMovementFlags1()) == 0)
            return false;

        if (condition.getMovementFlags2() != 0 && (player.getExtraUnitMovementFlags() & condition.getMovementFlags2()) == 0)
            return false;

        if (condition.getWeaponSubclassMask() != 0) {
            var mainHand = player.getItemByPos(INVENTORY_SLOT_BAG_0, EquipmentSlot.MAINHAND);
            if (!mainHand || !((1 << mainHand -> GetTemplate()->GetSubClass()) &condition -> WeaponSubclassMask))
            return false;
        }

        if (condition -> PartyStatus) {
            Group const*group = player -> GetGroup();
            switch (condition -> PartyStatus) {
                case 1:
                    if (group)
                        return false;
                    break;
                case 2:
                    if (!group)
                        return false;
                    break;
                case 3:
                    if (!group || group -> isRaidGroup())
                        return false;
                    break;
                case 4:
                    if (!group || !group -> isRaidGroup())
                        return false;
                    break;
                case 5:
                    if (group && group -> isRaidGroup())
                        return false;
                    break;
                default:
                    break;
            }
        }

        if (condition -> PrevQuestID[0]) {
            std::array < bool, std::tuple_size_v < decltype(condition -> PrevQuestID) >> results;
            results.fill(true);
            for (std::size_t i = 0; i < condition -> PrevQuestID.size() ;
            ++i)
            results[i] = player -> IsQuestCompletedBitSet(condition -> PrevQuestID[i]);

            if (!PlayerConditionLogic(condition -> PrevQuestLogic, results))
                return false;
        }

        if (condition -> CurrQuestID[0]) {
            std::array < bool, std::tuple_size_v < decltype(condition -> CurrQuestID) >> results;
            results.fill(true);
            for (std::size_t i = 0; i < condition -> CurrQuestID.size() ;
            ++i)
            if (condition -> CurrQuestID[i])
                results[i] = player -> FindQuestSlot(condition -> CurrQuestID[i]) != MAX_QUEST_LOG_SIZE;

            if (!PlayerConditionLogic(condition -> CurrQuestLogic, results))
                return false;
        }

        if (condition -> CurrentCompletedQuestID[0]) {
            std::array < bool, std::tuple_size_v < decltype(condition -> CurrentCompletedQuestID) >> results;
            results.fill(true);
            for (std::size_t i = 0; i < condition -> CurrentCompletedQuestID.size() ;
            ++i)
            if (condition -> CurrentCompletedQuestID[i])
                results[i] = player -> GetQuestStatus(condition -> CurrentCompletedQuestID[i]) == QUEST_STATUS_COMPLETE;

            if (!PlayerConditionLogic(condition -> CurrentCompletedQuestLogic, results))
                return false;
        }

        if (condition -> SpellID[0]) {
            std::array < bool, std::tuple_size_v < decltype(condition -> SpellID) >> results;
            results.fill(true);
            for (std::size_t i = 0; i < condition -> SpellID.size() ;
            ++i)
            if (condition -> SpellID[i])
                results[i] = player -> HasSpell(condition -> SpellID[i]);

            if (!PlayerConditionLogic(condition -> SpellLogic, results))
                return false;
        }

        if (condition -> ItemID[0]) {
            std::array < bool, std::tuple_size_v < decltype(condition -> ItemID) >> results;
            results.fill(true);
            for (std::size_t i = 0; i < condition -> ItemID.size() ;
            ++i)
            if (condition -> ItemID[i])
                results[i] = player -> GetItemCount(condition -> ItemID[i], condition -> ItemFlags != 0) >= condition -> ItemCount[i];

            if (!PlayerConditionLogic(condition -> ItemLogic, results))
                return false;
        }

        if (condition -> CurrencyID[0]) {
            std::array < bool, std::tuple_size_v < decltype(condition -> CurrencyID) >> results;
            results.fill(true);
            for (std::size_t i = 0; i < condition -> CurrencyID.size() ;
            ++i)
            if (condition -> CurrencyID[i])
                results[i] = player -> GetCurrencyQuantity(condition -> CurrencyID[i]) >= condition -> CurrencyCount[i];

            if (!PlayerConditionLogic(condition -> CurrencyLogic, results))
                return false;
        }

        if (condition -> Explored[0] || condition -> Explored[1]) {
            for (std::size_t i = 0; i < condition -> Explored.size() ;
            ++i)
            if (AreaTableEntry const*area = sAreaTableStore.LookupEntry(condition -> Explored[i]))
            if (!player -> HasExploredZone(area -> ID))
                return false;
        }

        if (condition -> AuraSpellID[0]) {
            std::array < bool, std::tuple_size_v < decltype(condition -> AuraSpellID) >> results;
            results.fill(true);
            for (std::size_t i = 0; i < condition -> AuraSpellID.size() ;
            ++i)
            {
                if (condition -> AuraSpellID[i]) {
                    if (condition -> AuraStacks[i])
                        results[i] = player -> GetAuraCount(condition -> AuraSpellID[i]) >= condition -> AuraStacks[i];
                    else
                        results[i] = player -> HasAura(condition -> AuraSpellID[i]);
                }
            }

            if (!PlayerConditionLogic(condition -> AuraSpellLogic, results))
                return false;
        }

        if (condition -> Time[0]) {
            WowTime time0;
            time0.SetPackedTime(condition -> Time[0]);

            if (condition -> Time[1]) {
                WowTime time1;
                time1.SetPackedTime(condition -> Time[1]);

                if (!GameTime::GetWowTime () -> IsInRange(time0, time1))
                return false;
            } else if (*GameTime::GetWowTime () != time0)
            return false;
        }

        if (condition -> WorldStateExpressionID) {
            WorldStateExpressionEntry const*
            worldStateExpression = sWorldStateExpressionStore.LookupEntry(condition -> WorldStateExpressionID);
            if (!worldStateExpression)
                return false;

            if (!IsMeetingWorldStateExpression(player -> GetMap(), worldStateExpression))
                return false;
        }

        if (condition -> WeatherID)
            if (player ->

                    GetMap()->

        GetZoneWeather(player ->

                GetZoneId()) !=

                WeatherState(condition -> WeatherID))
        return false;

        if (condition -> Achievement[0]) {
            std::array < bool, std::tuple_size_v < decltype(condition -> Achievement) >> results;
            results.fill(true);
            for (std::size_t i = 0; i < condition -> Achievement.size() ;
            ++i)
            {
                if (condition -> Achievement[i]) {
                    // if (condition->Flags & 2) { any character on account completed it } else { current character only }
                    // TODO: part of accountwide achievements
                    results[i] = player -> HasAchieved(condition -> Achievement[i]);
                }
            }

            if (!PlayerConditionLogic(condition -> AchievementLogic, results))
                return false;
        }

        if (condition -> LfgStatus[0]) {
            std::array < bool, std::tuple_size_v < decltype(condition -> LfgStatus) >> results;
            results.fill(true);
            for (std::size_t i = 0; i < condition -> LfgStatus.size() ;
            ++i)
            if (condition -> LfgStatus[i])
                results[i] = PlayerConditionCompare(condition -> LfgCompare[i],
                        GetPlayerConditionLfgValue(player, PlayerConditionLfgStatus(condition -> LfgStatus[i])),
                        condition -> LfgValue[i]);

            if (!PlayerConditionLogic(condition -> LfgLogic, results))
                return false;
        }

        if (condition -> AreaID[0]) {
            std::array < bool, std::tuple_size_v < decltype(condition -> AreaID) >> results;
            results.fill(true);
            for (std::size_t i = 0; i < condition -> AreaID.size() ;
            ++i)
            if (condition -> AreaID[i])
                results[i] = DB2Manager::IsInArea (player -> GetAreaId(), condition -> AreaID[i]);

            if (!PlayerConditionLogic(condition -> AreaLogic, results))
                return false;
        }

        if (condition -> MinExpansionLevel != -1 && player ->

                GetSession()->

        GetExpansion() < condition -> MinExpansionLevel)
        return false;

        if (condition -> MaxExpansionLevel != -1 && player ->

                GetSession()->

        GetExpansion() > condition -> MaxExpansionLevel)
        return false;

        if (condition -> MinExpansionLevel != -1 && condition -> MinExpansionTier != -1 && !player ->

                IsGameMaster()
                        && ((condition -> MinExpansionLevel ==

                        int32(sWorld ->

                                getIntConfig(CONFIG_EXPANSION)) && condition -> MinExpansionTier > 0) /*TODO: implement tier*/
                        || condition -> MinExpansionLevel >

                        int32(sWorld ->

                                getIntConfig(CONFIG_EXPANSION))))
            return false;

        if (condition -> PhaseID || condition -> PhaseGroupID || condition -> PhaseUseFlags)
            if (!PhasingHandler::

                    InDbPhaseShift
        (player, condition -> PhaseUseFlags, condition -> PhaseID, condition -> PhaseGroupID))
        return false;

        if (condition -> QuestKillID) {
            Quest const*quest = sObjectMgr -> GetQuestTemplate(condition -> QuestKillID);
            uint16 questSlot = player -> FindQuestSlot(condition -> QuestKillID);
            if (quest && player -> GetQuestStatus(condition -> QuestKillID) != QUEST_STATUS_COMPLETE && questSlot < MAX_QUEST_LOG_SIZE) {
                std::array < bool, std::tuple_size_v < decltype(condition -> QuestKillMonster) >> results;
                results.fill(true);
                for (std::size_t i = 0; i < condition -> QuestKillMonster.size() ;
                ++i)
                {
                    if (condition -> QuestKillMonster[i]) {
                        auto objectiveItr = std::find_if
                        (quest -> GetObjectives().begin(), quest -> GetObjectives().end(), [condition, i](QuestObjective const&
                        objective) ->bool
                        {
                            return objective.Type == QUEST_OBJECTIVE_MONSTER && uint32(objective.ObjectID) == condition -> QuestKillMonster[i];
                        });
                        if (objectiveItr != quest -> GetObjectives().end())
                            results[i] = player -> GetQuestSlotObjectiveData(questSlot, * objectiveItr) >=
                        objectiveItr -> Amount;
                    }
                }

                if (!PlayerConditionLogic(condition -> QuestKillLogic, results))
                    return false;
            }
        }

        if (condition -> MinAvgItemLevel &&

                int32(std::floor (player -> m_playerData -> AvgItemLevel[0])) <
        condition -> MinAvgItemLevel)
        return false;

        if (condition -> MaxAvgItemLevel &&

                int32(std::floor (player -> m_playerData -> AvgItemLevel[0]))>
        condition -> MaxAvgItemLevel)
        return false;

        if (condition -> MinAvgEquippedItemLevel &&

                uint32(std::floor (player -> m_playerData -> AvgItemLevel[1])) <
        condition -> MinAvgEquippedItemLevel)
        return false;

        if (condition -> MaxAvgEquippedItemLevel &&

                uint32(std::floor (player -> m_playerData -> AvgItemLevel[1]))>
        condition -> MaxAvgEquippedItemLevel)
        return false;

        if (condition -> ModifierTreeID && !player ->

                ModifierTreeSatisfied(condition -> ModifierTreeID))
            return false;

        return true;
    }

    public static boolean isPlayerMeetingExpression(Player player, WorldStateExpression expression) {
        ByteBuffer buffer = new byteBuffer(expression.Expression.ToByteArray());

        if (buffer.getSize() == 0) {
            return false;
        }

        var enabled = buffer.ReadBool();

        if (!enabled) {
            return false;
        }

        var finalResult = evalRelOp(buffer, player);
        var resultLogic = WorldStateExpressionLogic.forValue(buffer.readUInt8());

        while (resultLogic != WorldStateExpressionLogic.NONE) {
            var secondResult = evalRelOp(buffer, player);

            switch (resultLogic) {
                case And:
                    finalResult = finalResult && secondResult;

                    break;
                case Or:
                    finalResult = finalResult || secondResult;

                    break;
                case Xor:
                    finalResult = finalResult != secondResult;

                    break;
                default:
                    break;
            }

            if (buffer.GetCurrentStream().position < buffer.getSize()) {
                break;
            }

            resultLogic = WorldStateExpressionLogic.forValue(buffer.readUInt8());
        }

        return finalResult;
    }

    public static boolean isUnitMeetingCondition(Unit unit, Unit otherUnit, UnitConditionRecord condition) {
        for (var i = 0; i < condition.Variable.length; ++i) {
            if (condition.Variable[i] == 0) {
                break;
            }

            var unitValue = getUnitConditionVariable(unit, otherUnit, UnitConditionVariable.forValue(condition.Variable[i]), condition.Value[i]);
            var meets = false;

            switch (UnitConditionOp.forValue(condition.Op[i])) {
                case EqualTo:
                    meets = unitValue == condition.Value[i];

                    break;
                case NotEqualTo:
                    meets = unitValue != condition.Value[i];

                    break;
                case LessThan:
                    meets = unitValue < condition.Value[i];

                    break;
                case LessThanOrEqualTo:
                    meets = unitValue <= condition.Value[i];

                    break;
                case GreaterThan:
                    meets = unitValue > condition.Value[i];

                    break;
                case GreaterThanOrEqualTo:
                    meets = unitValue >= condition.Value[i];

                    break;
                default:
                    break;
            }

            if (condition.getFlags().hasFlag(UnitConditionFlags.LogicOr)) {
                if (meets) {
                    return true;
                }
            } else if (!meets) {
                return false;
            }
        }

        return !condition.getFlags().hasFlag(UnitConditionFlags.LogicOr);
    }

    private static boolean playerConditionCompare(int comparisonType, int value1, int value2) {
        switch (comparisonType) {
            case 1:
                return value1 == value2;
            case 2:
                return value1 != value2;
            case 3:
                return value1 > value2;
            case 4:
                return value1 >= value2;
            case 5:
                return value1 < value2;
            case 6:
                return value1 <= value2;
            default:
                break;
        }

        return false;
    }

    private static boolean playerConditionLogic(int logic, boolean[] results) {
        for (var i = 0; i < results.length; ++i) {
            if ((boolean) ((logic >>> (16 + i)) & 1)) {
                results[i] ^= true;
            }
        }

        var result = results[0];

        for (var i = 1; i < results.length; ++i) {
            switch ((logic >>> (2 * (i - 1))) & 3) {
                case 1:
                    result = result && results[i];

                    break;
                case 2:
                    result = result || results[i];

                    break;
                default:
                    break;
            }
        }

        return result;
    }

    private static int getUnitConditionVariable(Unit unit, Unit otherUnit, UnitConditionVariable variable, int value) {
        switch (variable) {
            case Race:
                return unit.getRace().getValue();
            case Class:
                return unit.getUnitClass().getValue();
            case Level:
                return (int) unit.getLevel();
            case IsSelf:
                return unit == otherUnit ? 1 : 0;
            case IsMyPet:
                return (otherUnit != null && Objects.equals(unit.getCharmerOrOwnerGUID(), otherUnit.getGUID())) ? 1 : 0;
            case IsMaster:
                return (otherUnit && Objects.equals(otherUnit.getCharmerOrOwnerGUID(), unit.getGUID())) ? 1 : 0;
            case IsTarget:
                return (otherUnit && Objects.equals(otherUnit.getTarget(), unit.getGUID())) ? 1 : 0;
            case CanAssist:
                return (otherUnit && unit.isValidAssistTarget(otherUnit)) ? 1 : 0;
            case CanAttack:
                return (otherUnit && unit.isValidAttackTarget(otherUnit)) ? 1 : 0;
            case HasPet:
                return (!unit.getCharmedGUID().isEmpty() || !unit.getMinionGUID().isEmpty()) ? 1 : 0;
            case HasWeapon:
                var player = unit.toPlayer();

                if (player != null) {
                    return (player.getWeaponForAttack(WeaponAttackType.BaseAttack) || player.getWeaponForAttack(WeaponAttackType.OffAttack)) ? 1 : 0;
                }

                return (unit.getVirtualItemId(0) != 0 || unit.getVirtualItemId(1) != 0) ? 1 : 0;
            case HealthPct:
                return (int) unit.getHealthPct();
            case ManaPct:
                return (int) unit.getPowerPct(powerType.mana);
            case RagePct:
                return (int) unit.getPowerPct(powerType.Rage);
            case EnergyPct:
                return (int) unit.getPowerPct(powerType.Energy);
            case ComboPoints:
                return unit.getPower(powerType.ComboPoints);
            case HasHelpfulAuraSpell:
                return unit.getAppliedAurasQuery().hasSpellId(new integer(value)).hasNegitiveFlag(false).getResults().Any() ? value : 0;
            case HasHelpfulAuraDispelType:
                return unit.getAppliedAurasQuery().hasDispelType(DispelType.forValue(value)).hasNegitiveFlag(false).getResults().Any() ? value : 0;
            case HasHelpfulAuraMechanic:
                return unit.getAppliedAurasQuery().hasNegitiveFlag(false).alsoMatches(aurApp -> (aurApp.base.spellInfo.getSpellMechanicMaskByEffectMask(aurApp.effectMask) & (1 << value)) != 0).getResults().Any() ? value : 0;
            case HasHarmfulAuraSpell:
                return unit.getAppliedAurasQuery().hasSpellId(new integer(value)).hasNegitiveFlag().getResults().Any() ? value : 0;
            case HasHarmfulAuraDispelType:
                return unit.getAppliedAurasQuery().hasDispelType(DispelType.forValue(value)).hasNegitiveFlag().getResults().Any() ? value : 0;
            case HasHarmfulAuraMechanic:
                return unit.getAppliedAurasQuery().hasNegitiveFlag().alsoMatches(aurApp -> (aurApp.base.spellInfo.getSpellMechanicMaskByEffectMask(aurApp.effectMask) & (1 << value)) != 0).getResults().Any() ? value : 0;
            case HasHarmfulAuraSchool:
                return unit.getAppliedAurasQuery().hasNegitiveFlag().alsoMatches(aurApp -> ((int) aurApp.base.spellInfo.getSchoolMask() & (1 << value)) != 0).getResults().Any() ? value : 0;
            case DamagePhysicalPct:
                break;
            case DamageHolyPct:
                break;
            case DamageFirePct:
                break;
            case DamageNaturePct:
                break;
            case DamageFrostPct:
                break;
            case DamageShadowPct:
                break;
            case DamageArcanePct:
                break;
            case InCombat:
                return unit.isInCombat() ? 1 : 0;
            case IsMoving:
                return unit.hasUnitMovementFlag(MovementFlag.Forward.getValue() | MovementFlag.Backward.getValue().getValue() | MovementFlag.StrafeLeft.getValue().getValue().getValue() | MovementFlag.StrafeRight.getValue().getValue().getValue()) ? 1 : 0;
            case IsCasting:
            case IsCastingSpell: // this is supposed to return spell id by client code but data always has 0 or 1
                return unit.getCurrentSpell(CurrentSpellTypes.generic) != null ? 1 : 0;
            case IsChanneling:
            case IsChannelingSpell: // this is supposed to return spell id by client code but data always has 0 or 1
                return unit.getChannelSpellId() != 0 ? 1 : 0;
            case NumberOfMeleeAttackers:
                return unit.getAttackers().size() (attacker ->
            {
                var distance = Math.max(unit.getCombatReach() + attacker.combatReach + 1.3333334f, 5.0f);

                if (unit.hasUnitFlag(UnitFlag.PlayerControlled) || attacker.hasUnitFlag(UnitFlag.PlayerControlled)) {
                    distance += 1.0f;
                }

                return unit.getLocation().getExactDistSq(attacker.location) < distance * distance;
            });
            case IsAttackingMe:
                return (otherUnit != null && Objects.equals(unit.getTarget(), otherUnit.getGUID())) ? 1 : 0;
            case Range:
                return otherUnit ? (int) unit.getLocation().getExactDist(otherUnit.getLocation()) : 0;
            case InMeleeRange:
                if (otherUnit) {
                    var distance = Math.max(unit.getCombatReach() + otherUnit.getCombatReach() + 1.3333334f, 5.0f);

                    if (unit.hasUnitFlag(UnitFlag.PlayerControlled) || otherUnit.hasUnitFlag(UnitFlag.PlayerControlled)) {
                        distance += 1.0f;
                    }

                    return (unit.getLocation().getExactDistSq(otherUnit.getLocation()) < distance * distance) ? 1 : 0;
                }

                return 0;
            case PursuitTime:
                break;
            case HasHarmfulAuraCanceledByDamage:
                return unit.hasNegativeAuraWithInterruptFlag(SpellAuraInterruptFlags.damage) ? 1 : 0;
            case HasHarmfulAuraWithPeriodicDamage:
                return unit.hasAuraType(AuraType.PeriodicDamage) ? 1 : 0;
            case NumberOfEnemies:
                return unit.getThreatManager().getThreatListSize();
            case NumberOfFriends:
                break;
            case ThreatPhysicalPct:
                break;
            case ThreatHolyPct:
                break;
            case ThreatFirePct:
                break;
            case ThreatNaturePct:
                break;
            case ThreatFrostPct:
                break;
            case ThreatShadowPct:
                break;
            case ThreatArcanePct:
                break;
            case IsInterruptible:
                break;
            case NumberOfAttackers:
                return unit.getAttackers().size();
            case NumberOfRangedAttackers:
                return unit.getAttackers().size() (attacker ->
            {
                var distance = Math.max(unit.getCombatReach() + attacker.combatReach + 1.3333334f, 5.0f);

                if (unit.hasUnitFlag(UnitFlag.PlayerControlled) || attacker.hasUnitFlag(UnitFlag.PlayerControlled)) {
                    distance += 1.0f;
                }

                return unit.getLocation().getExactDistSq(attacker.location) >= distance * distance;
            });
            case CreatureType:
                return unit.getCreatureType().getValue();
            case IsMeleeAttacking: {
                var target = global.getObjAccessor().GetUnit(unit, unit.getTarget());

                if (target != null) {
                    var distance = Math.max(unit.getCombatReach() + target.getCombatReach() + 1.3333334f, 5.0f);

                    if (unit.hasUnitFlag(UnitFlag.PlayerControlled) || target.hasUnitFlag(UnitFlag.PlayerControlled)) {
                        distance += 1.0f;
                    }

                    return (unit.getLocation().getExactDistSq(target.getLocation()) < distance * distance) ? 1 : 0;
                }

                return 0;
            }
            case IsRangedAttacking: {
                var target = global.getObjAccessor().GetUnit(unit, unit.getTarget());

                if (target != null) {
                    var distance = Math.max(unit.getCombatReach() + target.getCombatReach() + 1.3333334f, 5.0f);

                    if (unit.hasUnitFlag(UnitFlag.PlayerControlled) || target.hasUnitFlag(UnitFlag.PlayerControlled)) {
                        distance += 1.0f;
                    }

                    return (unit.getLocation().getExactDistSq(target.getLocation()) >= distance * distance) ? 1 : 0;
                }

                return 0;
            }
            case Health:
                return (int) unit.getHealth();
            case SpellKnown:
                return unit.hasSpell((int) value) ? value : 0;
            case HasHarmfulAuraEffect:
                return (value >= 0 && value < AuraType.Total.getValue() && unit.getAuraEffectsByType(AuraType.forValue(value)).Any(aurEff -> aurEff.base.getApplicationOfTarget(unit.getGUID()).flags.hasFlag(AuraFlags.NEGATIVE))) ? 1 : 0;
            case IsImmuneToAreaOfEffect:
                break;
            case IsPlayer:
                return unit.isPlayer() ? 1 : 0;
            case DamageMagicPct:
                break;
            case DamageTotalPct:
                break;
            case ThreatMagicPct:
                break;
            case ThreatTotalPct:
                break;
            case HasCritter:
                return unit.getCritterGUID().isEmpty() ? 0 : 1;
            case HasTotemInSlot1:
                return unit.getSummonSlot()[SummonSlot.totem.getValue()].isEmpty() ? 0 : 1;
            case HasTotemInSlot2:
                return unit.getSummonSlot()[SummonSlot.Totem2.getValue()].isEmpty() ? 0 : 1;
            case HasTotemInSlot3:
                return unit.getSummonSlot()[SummonSlot.Totem3.getValue()].isEmpty() ? 0 : 1;
            case HasTotemInSlot4:
                return unit.getSummonSlot()[SummonSlot.Totem4.getValue()].isEmpty() ? 0 : 1;
            case HasTotemInSlot5:
                break;
            case Creature:
                return (int) unit.getEntry();
            case StringID:
                break;
            case HasAura:
                return unit.hasAura((int) value) ? value : 0;
            case IsEnemy:
                return (otherUnit && unit.getReactionTo(otherUnit) <= ReputationRank.Hostile.getValue()) ? 1 : 0;
            case IsSpecMelee:
                return (unit.isPlayer() && unit.toPlayer().getPrimarySpecialization() != 0 && CliDB.ChrSpecializationStorage.get(unit.toPlayer().getPrimarySpecialization()).flags.hasFlag(ChrSpecializationFlag.Melee)) ? 1 : 0;
            case IsSpecTank:
                return (unit.isPlayer() && unit.toPlayer().getPrimarySpecialization() != 0 && CliDB.ChrSpecializationStorage.get(unit.toPlayer().getPrimarySpecialization()).role == 0) ? 1 : 0;
            case IsSpecRanged:
                return (unit.isPlayer() && unit.toPlayer().getPrimarySpecialization() != 0 && CliDB.ChrSpecializationStorage.get(unit.toPlayer().getPrimarySpecialization()).flags.hasFlag(ChrSpecializationFlag.Ranged)) ? 1 : 0;
            case IsSpecHealer:
                return (unit.isPlayer() && unit.toPlayer().getPrimarySpecialization() != 0 && CliDB.ChrSpecializationStorage.get(unit.toPlayer().getPrimarySpecialization()).role == 1) ? 1 : 0;
            case IsPlayerControlledNPC:
                return unit.isCreature() && unit.hasUnitFlag(UnitFlag.PlayerControlled) ? 1 : 0;
            case IsDying:
                return unit.getHealth() == 0 ? 1 : 0;
            case PathFailCount:
                break;
            case IsMounted:
                return unit.getMountDisplayId() != 0 ? 1 : 0;
            case Label:
                break;
            case IsMySummon:
                return (otherUnit && (Objects.equals(otherUnit.getCharmerGUID(), unit.getGUID()) || Objects.equals(otherUnit.getCreatorGUID(), unit.getGUID()))) ? 1 : 0;
            case IsSummoner:
                return (otherUnit && (Objects.equals(unit.getCharmerGUID(), otherUnit.getGUID()) || Objects.equals(unit.getCreatorGUID(), otherUnit.getGUID()))) ? 1 : 0;
            case IsMyTarget:
                return (otherUnit && Objects.equals(unit.getTarget(), otherUnit.getGUID())) ? 1 : 0;
            case Sex:
                return unit.getGender().getValue();
            case LevelWithinContentTuning:
                var levelRange = global.getDB2Mgr().GetContentTuningData((int) value, 0);

                if (levelRange != null) {
                    return unit.getLevel() >= levelRange.getValue().minLevel && unit.getLevel() <= levelRange.getValue().MaxLevel ? value : 0;
                }

                return 0;
            case IsFlying:
                return unit.isFlying() ? 1 : 0;
            case IsHovering:
                return unit.isHovering() ? 1 : 0;
            case HasHelpfulAuraEffect:
                return (value >= 0 && value < AuraType.Total.getValue() && unit.getAuraEffectsByType(AuraType.forValue(value)).Any(aurEff -> !aurEff.base.getApplicationOfTarget(unit.getGUID()).flags.hasFlag(AuraFlags.NEGATIVE))) ? 1 : 0;
            case HasHelpfulAuraSchool:
                return unit.getAppliedAurasQuery().hasNegitiveFlag().alsoMatches(aurApp -> ((int) aurApp.base.spellInfo.getSchoolMask() & (1 << value)) != 0).getResults().Any() ? 1 : 0;
            default:
                break;
        }

        return 0;
    }

    private static int evalSingleValue(ByteBuffer buffer, Player player) {
        var valueType = WorldStateExpressionValueType.forValue(buffer.readUInt8());
        var value = 0;

        switch (valueType) {
            case Constant: {
                value = buffer.readInt32();

                break;
            }
            case WorldState: {
                var worldStateId = buffer.readUInt();
                value = global.getWorldStateMgr().getValue((int) worldStateId, player.getMap());

                break;
            }
            case Function: {
                var functionType = WorldStateExpressionFunction.forValue(buffer.readUInt());
                var arg1 = evalSingleValue(buffer, player);
                var arg2 = evalSingleValue(buffer, player);

                if (functionType.getValue() >= WorldStateExpressionFunction.max.getValue()) {
                    return 0;
                }

                value = worldStateExpressionFunction(functionType, player, arg1, arg2);

                break;
            }
            default:
                break;
        }

        return value;
    }

    private static int worldStateExpressionFunction(WorldStateExpressionFunction functionType, Player player, int arg1, int arg2) {
        switch (functionType) {
            case RANDOM:
                return (int) RandomUtil.URand(Math.min(arg1, arg2), Math.max(arg1, arg2));
            case MONTH:
                return gameTime.GetDateAndTime().getMonthValue() + 1;
            case DAY:
                return gameTime.GetDateAndTime().getDayOfMonth() + 1;
            case TIME_OF_DAY:
                var localTime = gameTime.GetDateAndTime();

                return localTime.getHour() * time.Minute + localTime.getMinute();
            case REGION:
                return global.getWorldMgr().getRealmId().Region;
            case ClockHour:
                var currentHour = gameTime.GetDateAndTime().getHour() + 1;

                return currentHour <= 12 ? (currentHour != 0 ? currentHour : 12) : currentHour - 12;
            case OLD_DIFFICULTY_ID:
                var difficulty = CliDB.DifficultyStorage.get(player.getMap().getDifficultyID());

                if (difficulty != null) {
                    return difficulty.OldEnumValue;
                }

                return -1;
            case HOLIDAY_ACTIVE:
                return global.getGameEventMgr().isHolidayActive(HolidayIds.forValue(arg1)) ? 1 : 0;
            case TIMER_CURRENT_TIME:
                return (int) GameTime.getGameTime();
            case WEEK_NUMBER:
                var now = GameTime.getGameTime();
                int raidOrigin = 1135695600;
                var region = CliDB.CfgRegionsStorage.get(global.getWorldMgr().getRealmId().Region);

                if (region != null) {
                    raidOrigin = region.Raidorigin;
                }

                return (int) (now - raidOrigin) / time.Week;
            case DIFFICULTY_ID:
                return player.getMap().getDifficultyID().getValue();
            case WAR_MODE_ACTIVE:
                return player.hasPlayerFlag(playerFlags.WarModeActive) ? 1 : 0;
            case WORLD_STATE_EXPRESSION:
                var worldStateExpression = CliDB.WorldStateExpressionStorage.get(arg1);

                if (worldStateExpression != null) {
                    return isPlayerMeetingExpression(player, worldStateExpression) ? 1 : 0;
                }

                return 0;
            case MERSENNE_RANDOM:
                if (arg1 == 1) {
                    return 1;
                }

                //todo fix me
                // init with predetermined seed
                //std::mt19937 mt(arg2? arg2 : 1);
                //value = mt() % arg1 + 1;
                return 0;
            case NONE:
            case HOLIDAY_START:
            case HOLIDAY_LEFT:
            case UNK13:
            case UNK14:
            case UNK17:
            case UNK18:
            case UNK19:
            case UNK20:
            case UNK21:
            case KEYSTONE_AFFIX:
            case UNK24:
            case UNK25:
            case UNK26:
            case UNK27:
            case KEYSTONE_LEVEL:
            case UNK29:
            case UNK30:
            case UNK31:
            case UNK32:
            case UNK34:
            case UNK35:
            case UNK36:
            case UI_WIDGET_DATA:
            case TIME_EVENT_PASSED:
            default:
                return 0;
        }
    }

    private static int evalValue(ByteBuffer buffer, Player player) {
        var leftValue = evalSingleValue(buffer, player);

        var operatorType = WorldStateExpressionOperatorType.forValue(buffer.readUInt8());

        if (operatorType == WorldStateExpressionOperatorType.NONE) {
            return leftValue;
        }

        var rightValue = evalSingleValue(buffer, player);

        switch (operatorType) {
            case Sum:
                return leftValue + rightValue;
            case Substraction:
                return leftValue - rightValue;
            case Multiplication:
                return leftValue * rightValue;
            case Division:
                return rightValue == 0 ? 0 : leftValue / rightValue;
            case Remainder:
                return rightValue == 0 ? 0 : leftValue % rightValue;
            default:
                break;
        }

        return leftValue;
    }

    private static boolean evalRelOp(ByteBuffer buffer, Player player) {
        var leftValue = evalValue(buffer, player);

        var compareLogic = WorldStateExpressionComparisonType.forValue(buffer.readUInt8());

        if (compareLogic == WorldStateExpressionComparisonType.NONE) {
            return leftValue != 0;
        }

        var rightValue = evalValue(buffer, player);

        switch (compareLogic) {
            case Equal:
                return leftValue == rightValue;
            case NotEqual:
                return leftValue != rightValue;
            case Less:
                return leftValue < rightValue;
            case LessOrEqual:
                return leftValue <= rightValue;
            case Greater:
                return leftValue > rightValue;
            case GreaterOrEqual:
                return leftValue >= rightValue;
            default:
                break;
        }

        return false;
    }

    public GridMapTypeMask getSearcherTypeMaskForConditionList(ArrayList<Condition> conditions) {
        if (conditions.isEmpty()) {
            return GridMapTypeMask.All;
        }

        //     groupId, typeMask
        HashMap<Integer, GridMapTypeMask> elseGroupSearcherTypeMasks = new HashMap<Integer, GridMapTypeMask>();

        for (var i : conditions) {
            // group not filled yet, fill with widest mask possible
            if (!elseGroupSearcherTypeMasks.containsKey(i.elseGroup)) {
                elseGroupSearcherTypeMasks.put(i.elseGroup, GridMapTypeMask.All);
            }
            // no point of checking anymore, empty mask
            else if (elseGroupSearcherTypeMasks.get(i.elseGroup).equals(0)) {
                continue;
            }

            if (i.referenceId != 0) // handle reference
            {
                var refe = conditionReferenceStorage.get(i.referenceId);
                elseGroupSearcherTypeMasks.put(i.elseGroup, elseGroupSearcherTypeMasks.get(i.elseGroup).getValue() & getSearcherTypeMaskForConditionList(refe).getValue());
            } else // handle normal condition
            {
                // object will match conditions in one ElseGroupStore only when it matches all of them
                // so, let's find a smallest possible mask which satisfies all conditions
                elseGroupSearcherTypeMasks.put(i.elseGroup, elseGroupSearcherTypeMasks.get(i.elseGroup).getValue() & i.getSearcherTypeMaskForCondition().getValue());
            }
        }

        // object will match condition when one of the checks in ElseGroupStore is matching
        // so, let's include all possible masks
        GridMapTypeMask mask = GridMapTypeMask.forValue(0);

        for (var i : elseGroupSearcherTypeMasks.entrySet()) {
            mask = GridMapTypeMask.forValue(mask.getValue() | i.getValue().getValue());
        }

        return mask;
    }

    public boolean isObjectMeetToConditionList(ConditionSourceInfo sourceInfo, List<Condition> conditions) {
        //     groupId, groupCheckPassed
        Map<Integer, Boolean> elseGroupStore = new HashMap<Integer, Boolean>();

        for (var condition : conditions) {
            Logs.CONDITION.debug("ConditionMgr::IsPlayerMeetToConditionList {} val1: {}", condition, condition.conditionValue1);

            if (condition.isLoaded()) {

                //! Find ElseGroup in ElseGroupStore
                elseGroupStore.putIfAbsent(condition.elseGroup, true);
                Boolean groupStatus = elseGroupStore.get(condition.elseGroup);

                if (!groupStatus) //! If another condition in this group was unmatched before this, don't bother checking (the group is false anyway)
                    continue;

                if (condition.referenceId != 0)//handle reference
                {
                    var ref = conditionStore.get(ConditionSourceType.REFERENCE_CONDITION).get(ConditionId.of(condition.referenceId, 0, 0));
                    if (ref != null) {
                        boolean condMeets = isObjectMeetToConditionList(sourceInfo, ref);
                        if (condition.negativeCondition)
                            condMeets = !condMeets;

                        if (!condMeets)
                            elseGroupStore.put(condition.elseGroup, false);
                    } else {
                        Logs.CONDITION.debug("ConditionMgr::IsPlayerMeetToConditionList {} Reference template -{} not found",
                                condition, condition.referenceId); // checked at loading, should never happen
                    }

                } else //handle normal condition
                {
                    if (!Conditions.condition(condition).test(sourceInfo))
                        elseGroupStore.put(condition.elseGroup, false);

                }
            }
        }

        for (var i : elseGroupStore.entrySet()) {
            if (i.getValue()) {
                return true;
            }
        }

        return false;
    }

    public boolean isObjectMeetToConditions(WorldObject obj, List<Condition> conditions) {
        ConditionSourceInfo srcInfo = new ConditionSourceInfo(obj);

        return isObjectMeetToConditions(srcInfo, conditions);
    }

    public boolean isObjectMeetToConditions(WorldObject obj1, WorldObject obj2, List<Condition> conditions) {
        ConditionSourceInfo srcInfo = new ConditionSourceInfo(obj1, obj2);

        return isObjectMeetToConditions(srcInfo, conditions);
    }

    public boolean isObjectMeetToConditions(ConditionSourceInfo sourceInfo, List<Condition> conditions) {
        if (conditions.isEmpty()) {
            return true;
        }
        Logs.CONDITION.debug("ConditionMgr::IsObjectMeetToConditions");
        return isObjectMeetToConditionList(sourceInfo, conditions);
    }

    public boolean canHaveSourceGroupSet(ConditionSourceType sourceType) {
        return sourceType == ConditionSourceType.CREATURE_LOOT_TEMPLATE
                || sourceType == ConditionSourceType.DISENCHANT_LOOT_TEMPLATE
                || sourceType == ConditionSourceType.FISHING_LOOT_TEMPLATE
                || sourceType == ConditionSourceType.GAME_OBJECT_LOOT_TEMPLATE
                || sourceType == ConditionSourceType.ITEM_LOOT_TEMPLATE
                || sourceType == ConditionSourceType.MAIL_LOOT_TEMPLATE
                || sourceType == ConditionSourceType.MILLING_LOOT_TEMPLATE
                || sourceType == ConditionSourceType.PICKPOCKETING_LOOT_TEMPLATE
                || sourceType == ConditionSourceType.PROSPECTING_LOOT_TEMPLATE
                || sourceType == ConditionSourceType.REFERENCE_LOOT_TEMPLATE
                || sourceType == ConditionSourceType.SKINNING_LOOT_TEMPLATE
                || sourceType == ConditionSourceType.SPELL_LOOT_TEMPLATE
                || sourceType == ConditionSourceType.GOSSIP_MENU
                || sourceType == ConditionSourceType.GOSSIP_MENU_OPTION
                || sourceType == ConditionSourceType.VEHICLE_SPELL
                || sourceType == ConditionSourceType.SPELL_IMPLICIT_TARGET
                || sourceType == ConditionSourceType.SPELL_CLICK_EVENT
                || sourceType == ConditionSourceType.SMART_EVENT
                || sourceType == ConditionSourceType.NPC_VENDOR
                || sourceType == ConditionSourceType.PHASE
                || sourceType == ConditionSourceType.AREA_TRIGGER
                || sourceType == ConditionSourceType.TRAINER_SPELL
                || sourceType == ConditionSourceType.OBJECT_ID_VISIBILITY;
    }

    public boolean canHaveSourceIdSet(ConditionSourceType sourceType) {
        return (sourceType == ConditionSourceType.SMART_EVENT);
    }

    public boolean isObjectMeetingNotGroupedConditions(ConditionSourceType sourceType, int entry, ConditionSourceInfo sourceInfo) {
        if (sourceType != ConditionSourceType.NONE) {
            var conditions = conditionStore.get(sourceType).get(ConditionId.of(0, entry, 0));
            if (!conditions.isEmpty()) {
                Logs.CONDITION.debug("GetConditionsForNotGroupedEntry: found conditions for type {} and entry {}", sourceType, entry);
                return isObjectMeetToConditions(sourceInfo, conditions);
            }
        }

        return true;
    }

    public boolean isObjectMeetingNotGroupedConditions(ConditionSourceType sourceType, int entry, WorldObject target0, WorldObject target1) {
        return isObjectMeetingNotGroupedConditions(sourceType, entry, target0, target1, null);
    }

    public boolean isObjectMeetingNotGroupedConditions(ConditionSourceType sourceType, int entry, WorldObject target0) {
        return isObjectMeetingNotGroupedConditions(sourceType, entry, target0, null, null);
    }

    public boolean isObjectMeetingNotGroupedConditions(ConditionSourceType sourceType, int entry, WorldObject target0, WorldObject target1, WorldObject target2) {
        ConditionSourceInfo conditionSource = new ConditionSourceInfo(target0, target1, target2);

        return isObjectMeetingNotGroupedConditions(sourceType, entry, conditionSource);
    }

    public boolean isMapMeetingNotGroupedConditions(ConditionSourceType sourceType, int entry, Map map) {
        ConditionSourceInfo conditionSource = new ConditionSourceInfo(map);

        return isObjectMeetingNotGroupedConditions(sourceType, entry, conditionSource);
    }

    public boolean hasConditionsForNotGroupedEntry(ConditionSourceType sourceType, int entry) {
        if (sourceType != ConditionSourceType.NONE) {
            return conditionStore.get(sourceType).containsKey(new ConditionId(0, entry, 0));
        }

        return false;
    }

    public boolean isObjectMeetingSpellClickConditions(int creatureId, int spellId, WorldObject clicker, WorldObject target) {
        var conditions = conditionStore.get(ConditionSourceType.SPELL_CLICK_EVENT).get(new ConditionId(creatureId, spellId, 0));
        if (conditions != null) {
            Logs.CONDITION.debug("IsObjectMeetingSpellClickConditions: found conditions for SpellClickEvent entry {} spell {}", creatureId, spellId);
            ConditionSourceInfo sourceInfo = new ConditionSourceInfo(clicker, target);
            return isObjectMeetToConditions(sourceInfo, conditions);
        }
        return true;
    }

    public ArrayList<Condition> getConditionsForSpellClickEvent(int creatureId, int spellId) {
        var conditions = conditionStore.get(ConditionSourceType.SPELL_CLICK_EVENT).get(new ConditionId(creatureId, spellId, 0));
        if (multiMap != null) {
            var conditions = multiMap.get(spellId);

            if (!conditions.isEmpty()) {
                Log.outDebug(LogFilter.condition, "GetConditionsForSpellClickEvent: found conditions for SpellClickEvent entry {} spell {}", creatureId, spellId);

                return conditions;
            }
        }

        return null;
    }

    public boolean isObjectMeetingVehicleSpellConditions(int creatureId, int spellId, Player player, Unit vehicle) {
        var multiMap = vehicleSpellConditionStorage.get(creatureId);

        if (multiMap != null) {
            var conditions = multiMap.get(spellId);

            if (!conditions.isEmpty()) {
                Log.outDebug(LogFilter.condition, "GetConditionsForVehicleSpell: found conditions for Vehicle entry {} spell {}", creatureId, spellId);
                ConditionSourceInfo sourceInfo = new ConditionSourceInfo(player, vehicle);

                return isObjectMeetToConditions(sourceInfo, conditions);
            }
        }

        return true;
    }

    public boolean isObjectMeetingSmartEventConditions(long entryOrGuid, int eventId, SmartScriptType sourceType, Unit unit, WorldObject baseObject) {
        var multiMap = smartEventConditionStorage.get(Tuple.create((int) entryOrGuid, (int) sourceType.getValue()));

        if (multiMap != null) {
            var conditions = multiMap.get(eventId + 1);

            if (!conditions.isEmpty()) {
                Log.outDebug(LogFilter.condition, "GetConditionsForSmartEvent: found conditions for Smart Event entry or guid {} eventId {}", entryOrGuid, eventId);
                ConditionSourceInfo sourceInfo = new ConditionSourceInfo(unit, baseObject);

                return isObjectMeetToConditions(sourceInfo, conditions);
            }
        }

        return true;
    }

    public boolean isObjectMeetingVendorItemConditions(int creatureId, int itemId, Player player, Creature vendor) {
        var multiMap = npcVendorConditionContainerStorage.get(creatureId);

        if (multiMap != null) {
            var conditions = multiMap.get(itemId);

            if (!conditions.isEmpty()) {
                Log.outDebug(LogFilter.condition, "GetConditionsForNpcVendor: found conditions for creature entry {} item {}", creatureId, itemId);
                ConditionSourceInfo sourceInfo = new ConditionSourceInfo(player, vendor);

                return isObjectMeetToConditions(sourceInfo, conditions);
            }
        }

        return true;
    }

    public boolean isSpellUsedInSpellClickConditions(int spellId) {
        return spellsUsedInSpellClickConditions.contains(spellId);
    }

    public ArrayList<Condition> getConditionsForAreaTrigger(int areaTriggerId, boolean isServerSide) {
        return areaTriggerConditionContainerStorage.get(Tuple.create(areaTriggerId, isServerSide));
    }

    public boolean isObjectMeetingTrainerSpellConditions(int trainerId, int spellId, Player player) {
        var multiMap = trainerSpellConditionContainerStorage.get(trainerId);

        if (multiMap != null) {
            var conditionList = multiMap.get(spellId);

            if (!conditionList.isEmpty()) {
                Log.outDebug(LogFilter.condition, String.format("GetConditionsForTrainerSpell: found conditions for trainer id %1$s spell %2$s", trainerId, spellId));

                return isObjectMeetToConditions(player, conditionList);
            }
        }

        return true;
    }

    public boolean isObjectMeetingVisibilityByObjectIdConditions(TypeId objectType, int entry, WorldObject seer) {
        var conditions = objectVisibilityConditionStorage.get((objectType, entry));

        if (conditions != null) {
            Log.outDebug(LogFilter.condition, String.format("IsObjectMeetingVisibilityByObjectIdConditions: found conditions for objectType %1$s entry %2$s", objectType, entry));

            return isObjectMeetToConditions(seer, conditions);
        }

        return true;
    }

    public void loadConditions() {
        loadConditions(false);
    }

    public void loadConditions(boolean isReload) {
        var oldMSTime = System.currentTimeMillis();

        clean();

        //must clear all custom handled cases (groupped types) before reload
        if (isReload) {
            Log.outInfo(LogFilter.Server, "Reseting Loot conditions...");
            LootStorage.CREATURE.resetConditions();
            LootStorage.FISHING.resetConditions();
            LootStorage.GAMEOBJECT.resetConditions();
            LootStorage.ITEMS.resetConditions();
            LootStorage.MAIL.resetConditions();
            LootStorage.MILLING.resetConditions();
            LootStorage.PICKPOCKETING.resetConditions();
            LootStorage.REFERENCE.resetConditions();
            LootStorage.SKINNING.resetConditions();
            LootStorage.DISENCHANT.resetConditions();
            LootStorage.PROSPECTING.resetConditions();
            LootStorage.SPELL.resetConditions();

            Log.outInfo(LogFilter.Server, "Re-Loading `gossip_menu` Table for conditions!");
            global.getObjectMgr().loadGossipMenu();

            Log.outInfo(LogFilter.Server, "Re-Loading `gossip_menu_option` Table for conditions!");
            global.getObjectMgr().loadGossipMenuItems();
            global.getSpellMgr().unloadSpellInfoImplicitTargetConditionLists();

            global.getObjectMgr().unloadPhaseConditions();
        }

        var result = DB.World.query("SELECT SourceTypeOrReferenceId, sourceGroup, sourceEntry, sourceId, elseGroup, ConditionTypeOrReference, conditionTarget, " + " conditionValue1, conditionValue2, conditionValue3, negativeCondition, errorType, errorTextId, ScriptName FROM conditions");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 conditions. DB table `conditions` is empty!");

            return;
        }

        int count = 0;

        do {
            Condition cond = new Condition();
            var iSourceTypeOrReferenceId = result.<Integer>Read(0);
            cond.sourceGroup = result.<Integer>Read(1);
            cond.sourceEntry = result.<Integer>Read(2);
            cond.sourceId = result.<Integer>Read(3);
            cond.elseGroup = result.<Integer>Read(4);
            var iConditionTypeOrReference = result.<Integer>Read(5);
            cond.conditionTarget = result.<Byte>Read(6);
            cond.conditionValue1 = result.<Integer>Read(7);
            cond.conditionValue2 = result.<Integer>Read(8);
            cond.conditionValue3 = result.<Integer>Read(9);
            cond.negativeCondition = result.<Byte>Read(10) != 0;
            cond.errorType = result.<Integer>Read(11);
            cond.errorTextId = result.<Integer>Read(12);
            cond.scriptId = global.getObjectMgr().getScriptId(result.<String>Read(13));

            if (iConditionTypeOrReference >= 0) {
                cond.conditionType = ConditionTypes.forValue(iConditionTypeOrReference);
            }

            if (iSourceTypeOrReferenceId >= 0) {
                cond.sourceType = ConditionSourceType.forValue(iSourceTypeOrReferenceId);
            }

            if (iConditionTypeOrReference < 0) //it has a reference
            {
                if (iConditionTypeOrReference == iSourceTypeOrReferenceId) //self referencing, skip
                {
                    Logs.SQL.error("Condition reference {} is referencing self, skipped", iSourceTypeOrReferenceId);

                    continue;
                }

                cond.referenceId = (int) Math.abs(iConditionTypeOrReference);

                var rowType = "reference template";

                if (iSourceTypeOrReferenceId >= 0) {
                    rowType = "reference";
                }

                //check for useless data
                if (cond.conditionTarget != 0) {
                    Logs.SQL.error("Condition {} {} has useless data in conditionTarget ({})!", rowType, iSourceTypeOrReferenceId, cond.conditionTarget);
                }

                if (cond.conditionValue1 != 0) {
                    Logs.SQL.error("Condition {} {} has useless data in value1 ({})!", rowType, iSourceTypeOrReferenceId, cond.conditionValue1);
                }

                if (cond.conditionValue2 != 0) {
                    Logs.SQL.error("Condition {} {} has useless data in value2 ({})!", rowType, iSourceTypeOrReferenceId, cond.conditionValue2);
                }

                if (cond.conditionValue3 != 0) {
                    Logs.SQL.error("Condition {} {} has useless data in value3 ({})!", rowType, iSourceTypeOrReferenceId, cond.conditionValue3);
                }

                if (cond.negativeCondition) {
                    Logs.SQL.error("Condition {} {} has useless data in negativeCondition ({})!", rowType, iSourceTypeOrReferenceId, cond.negativeCondition);
                }

                if (cond.sourceGroup != 0 && iSourceTypeOrReferenceId < 0) {
                    Logs.SQL.error("Condition {} {} has useless data in sourceGroup ({})!", rowType, iSourceTypeOrReferenceId, cond.sourceGroup);
                }

                if (cond.sourceEntry != 0 && iSourceTypeOrReferenceId < 0) {
                    Logs.SQL.error("Condition {} {} has useless data in sourceEntry ({})!", rowType, iSourceTypeOrReferenceId, cond.sourceEntry);
                }
            } else if (!isConditionTypeValid(cond)) //doesn't have reference, validate ConditionType
            {
                continue;
            }

            if (iSourceTypeOrReferenceId < 0) //it is a reference template
            {
                conditionReferenceStorage.add((int) Math.abs(iSourceTypeOrReferenceId), cond); //add to reference storage
                count++;

                continue;
            } //end of reference templates

            //if not a reference and SourceType is invalid, skip
            if (iConditionTypeOrReference >= 0 && !isSourceTypeValid(cond)) {
                continue;
            }

            //Grouping is only allowed for some types (loot templates, gossip menus, gossip items)
            if (cond.sourceGroup != 0 && !canHaveSourceGroupSet(cond.sourceType)) {
                Logs.SQL.error("{} has not allowed value of sourceGroup = {}!", cond, cond.sourceGroup);

                continue;
            }

            if (cond.sourceId != 0 && !canHaveSourceIdSet(cond.sourceType)) {
                Logs.SQL.error("{} has not allowed value of sourceId = {}!", cond, cond.sourceId);

                continue;
            }

            if (cond.errorType != 0 && cond.sourceType != ConditionSourceType.spell) {
                Logs.SQL.error("{} can't have errorType ({}), set to 0!", cond, cond.errorType);
                cond.errorType = 0;
            }

            if (cond.errorTextId != 0 && cond.errorType == 0) {
                Logs.SQL.error("{} has any errorType, errorTextId ({}) is set, set to 0!", cond, cond.errorTextId);
                cond.errorTextId = 0;
            }

            if (cond.sourceGroup != 0) {
                var valid = false;

                // handle grouped conditions
                switch (cond.sourceType) {
                    case CreatureLootTemplate:
                        valid = addToLootTemplate(cond, LootStorage.CREATURE.getLootForConditionFill(cond.sourceGroup));

                        break;
                    case DisenchantLootTemplate:
                        valid = addToLootTemplate(cond, LootStorage.DISENCHANT.getLootForConditionFill(cond.sourceGroup));

                        break;
                    case FishingLootTemplate:
                        valid = addToLootTemplate(cond, LootStorage.FISHING.getLootForConditionFill(cond.sourceGroup));

                        break;
                    case GameobjectLootTemplate:
                        valid = addToLootTemplate(cond, LootStorage.GAMEOBJECT.getLootForConditionFill(cond.sourceGroup));

                        break;
                    case ItemLootTemplate:
                        valid = addToLootTemplate(cond, LootStorage.items.getLootForConditionFill(cond.sourceGroup));

                        break;
                    case MailLootTemplate:
                        valid = addToLootTemplate(cond, LootStorage.MAIL.getLootForConditionFill(cond.sourceGroup));

                        break;
                    case MillingLootTemplate:
                        valid = addToLootTemplate(cond, LootStorage.MILLING.getLootForConditionFill(cond.sourceGroup));

                        break;
                    case PickpocketingLootTemplate:
                        valid = addToLootTemplate(cond, LootStorage.PICKPOCKETING.getLootForConditionFill(cond.sourceGroup));

                        break;
                    case ProspectingLootTemplate:
                        valid = addToLootTemplate(cond, LootStorage.PROSPECTING.getLootForConditionFill(cond.sourceGroup));

                        break;
                    case ReferenceLootTemplate:
                        valid = addToLootTemplate(cond, LootStorage.REFERENCE.getLootForConditionFill(cond.sourceGroup));

                        break;
                    case SkinningLootTemplate:
                        valid = addToLootTemplate(cond, LootStorage.SKINNING.getLootForConditionFill(cond.sourceGroup));

                        break;
                    case SpellLootTemplate:
                        valid = addToLootTemplate(cond, LootStorage.spell.getLootForConditionFill(cond.sourceGroup));

                        break;
                    case GossipMenu:
                        valid = addToGossipMenus(cond);

                        break;
                    case GossipMenuOption:
                        valid = addToGossipMenuItems(cond);

                        break;
                    case SpellClickEvent: {
                        if (!spellClickEventConditionStorage.containsKey(cond.sourceGroup)) {
                            spellClickEventConditionStorage.put(cond.sourceGroup, new MultiMap<Integer, condition>());
                        }

                        spellClickEventConditionStorage.get(cond.sourceGroup).add((int) cond.sourceEntry, cond);

                        if (cond.conditionType == ConditionTypes.aura) {
                            spellsUsedInSpellClickConditions.add(cond.conditionValue1);
                        }

                        ++count;

                        continue; // do not add to m_AllocatedMemory to avoid double deleting
                    }
                    case SpellImplicitTarget:
                        valid = addToSpellImplicitTargetConditions(cond);

                        break;
                    case VEHICLE_SPELL: {
                        if (!vehicleSpellConditionStorage.containsKey(cond.sourceGroup)) {
                            vehicleSpellConditionStorage.put(cond.sourceGroup, new MultiMap<Integer, condition>());
                        }

                        vehicleSpellConditionStorage.get(cond.sourceGroup).add((int) cond.sourceEntry, cond);
                        ++count;

                        continue; // do not add to m_AllocatedMemory to avoid double deleting
                    }
                    case SMART_EVENT: {
                        //! TODO: PAIR_32 ?
                        var key = Tuple.create(cond.sourceEntry, cond.sourceId);

                        if (!smartEventConditionStorage.containsKey(key)) {
                            smartEventConditionStorage.put(key, new MultiMap<Integer, condition>());
                        }

                        smartEventConditionStorage.get(key).add(cond.sourceGroup, cond);
                        ++count;

                        continue;
                    }
                    case NpcVendor: {
                        if (!npcVendorConditionContainerStorage.containsKey(cond.sourceGroup)) {
                            npcVendorConditionContainerStorage.put(cond.sourceGroup, new MultiMap<Integer, condition>());
                        }

                        npcVendorConditionContainerStorage.get(cond.sourceGroup).add((int) cond.sourceEntry, cond);
                        ++count;

                        continue;
                    }
                    case Phase:
                        valid = addToPhases(cond);

                        break;
                    case AreaTrigger:
                        areaTriggerConditionContainerStorage.add(Tuple.create(cond.sourceGroup, cond.sourceEntry != 0), cond);
                        ++count;

                        continue;
                    case TrainerSpell: {
                        if (!trainerSpellConditionContainerStorage.containsKey(cond.sourceGroup)) {
                            trainerSpellConditionContainerStorage.put(cond.sourceGroup, new MultiMap<Integer, condition>());
                        }

                        trainerSpellConditionContainerStorage.get(cond.sourceGroup).add((int) cond.sourceEntry, cond);
                        ++count;

                        continue;
                    }
                    case ObjectIdVisibility: {
                        objectVisibilityConditionStorage.add((cond.sourceGroup, (int) cond.sourceEntry), cond);
                        valid = true;
                        ++count;

                        continue;
                    }
                    default:
                        break;
                }

                if (!valid) {
                    Logs.SQL.error("{} Not handled grouped condition.", cond);
                } else {
                    ++count;
                }

                continue;
            }

            //add new Condition to storage based on Type/Entry
            if (cond.sourceType == ConditionSourceType.SpellClickEvent && cond.conditionType == ConditionTypes.aura) {
                spellsUsedInSpellClickConditions.add(cond.conditionValue1);
            }

            conditionStorage.get(cond.sourceType).add((int) cond.sourceEntry, cond);
            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {} conditions in {} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    private boolean canHaveConditionType(ConditionSourceType sourceType, ConditionType conditionType) {
        if (Objects.requireNonNull(sourceType) == ConditionSourceType.SPAWN_GROUP) {
            return switch (conditionType) {
                case NONE, ACTIVE_EVENT, INSTANCE_INFO, MAP_ID, WORLD_STATE, REALM_ACHIEVEMENT, DIFFICULTY_ID,
                     SCENARIO_STEP -> true;
                default -> false;
            };
        }

        return true;
    }

    private boolean addToLootTemplate(Condition cond, LootTemplate loot) {
        if (loot == null) {
            Logs.SQL.error("{} LootTemplate {} not found.", cond, cond.sourceGroup);

            return false;
        }

        if (loot.addConditionItem(cond)) {
            return true;
        }

        Logs.SQL.error("{} Item {} not found in LootTemplate {}.", cond, cond.sourceEntry, cond.sourceGroup);

        return false;
    }

    private boolean addToGossipMenus(Condition cond) {
        var pMenuBounds = global.getObjectMgr().getGossipMenusMapBounds(cond.sourceGroup);

        for (var menu : pMenuBounds) {
            if (menu.getMenuId() == cond.sourceGroup && menu.getTextId() == cond.sourceEntry) {
                menu.getConditions().add(cond);

                return true;
            }
        }

        Logs.SQL.error("{} GossipMenu {} not found.", cond, cond.sourceGroup);

        return false;
    }

    private boolean addToGossipMenuItems(Condition cond) {
        var pMenuItemBounds = global.getObjectMgr().getGossipMenuItemsMapBounds(cond.sourceGroup);

        for (var gossipMenuItem : pMenuItemBounds) {
            if (gossipMenuItem.getMenuId() == cond.sourceGroup && gossipMenuItem.getOrderIndex() == cond.sourceEntry) {
                gossipMenuItem.getConditions().add(cond);

                return true;
            }
        }

        Logs.SQL.error("{} GossipMenuId {} Item {} not found.", cond, cond.sourceGroup, cond.sourceEntry);

        return false;
    }

    private boolean addToSpellImplicitTargetConditions(Condition cond) {
        global.getSpellMgr().forEachSpellInfoDifficulty((int) cond.sourceEntry, spellInfo ->
        {
            var conditionEffMask = cond.sourceGroup;
            ArrayList<Integer> sharedMasks = new ArrayList<>();

            for (var spellEffectInfo : spellInfo.effects) {
                // additional checks by condition type
                if ((conditionEffMask & (1 << spellEffectInfo.effectIndex)) != 0) {
                    switch (cond.conditionType) {
                        case ObjectEntryGuid: {
                            var implicitTargetMask = SpellCastTargetFlags.forValue(spellInfo.getTargetFlagMask(spellEffectInfo.targetA.objectType).getValue() | spellInfo.getTargetFlagMask(spellEffectInfo.targetB.objectType).getValue());

                            if (implicitTargetMask.hasFlag(SpellCastTargetFlags.UnitMask) && cond.conditionValue1 != (int) TypeId.UNIT.getValue() && cond.conditionValue1 != (int) TypeId.PLAYER.getValue()) {
                                Logs.SQL.error(String.format("%1$s in `condition` table - spell %2$s EFFECT_%3$s - target requires ConditionValue1 to be either TYPEID_UNIT (%4$s) or TYPEID_PLAYER (%5$s)", cond, spellInfo.id, spellEffectInfo.effectIndex, (int) TypeId.UNIT.getValue(), (int) TypeId.PLAYER.getValue()));

                                return;
                            }

                            if (implicitTargetMask.hasFlag(SpellCastTargetFlags.GameobjectMask) && cond.conditionValue1 != (int) TypeId.gameObject.getValue()) {
                                Logs.SQL.error(String.format("%1$s in `condition` table - spell %2$s EFFECT_%3$s - target requires ConditionValue1 to be TYPEID_GAMEOBJECT (%4$s)", cond, spellInfo.id, spellEffectInfo.effectIndex, (int) TypeId.gameObject.getValue()));

                                return;
                            }

                            if (implicitTargetMask.hasFlag(SpellCastTargetFlags.CorpseMask) && cond.conditionValue1 != (int) TypeId.Corpse.getValue()) {
                                Logs.SQL.error(String.format("%1$s in `condition` table - spell %2$s EFFECT_%3$s - target requires ConditionValue1 to be TYPEID_CORPSE (%4$s)", cond, spellInfo.id, spellEffectInfo.effectIndex, (int) TypeId.Corpse.getValue()));

                                return;
                            }

                            break;
                        }
                        default:
                            break;
                    }
                }

                // check if effect is already a part of some shared mask
                if (sharedMasks.Any(mask -> !!(boolean) (mask & (1 << spellEffectInfo.effectIndex)))) {
                    continue;
                }

                // build new shared mask with found effect
                var sharedMask = (int) (1 << spellEffectInfo.effectIndex);
                var cmp = spellEffectInfo.implicitTargetConditions;

                for (var effIndex = spellEffectInfo.effectIndex + 1; effIndex < spellInfo.effects.count; ++effIndex) {
                    if (spellInfo.getEffect(effIndex).implicitTargetConditions == cmp) {
                        sharedMask |= (int) (1 << effIndex);
                    }
                }

                sharedMasks.add(sharedMask);
            }

            for (var effectMask : sharedMasks) {
                // some effect indexes should have same data
                var commonMask = (effectMask & conditionEffMask);

                if (commonMask != 0) {
                    byte firstEffIndex = 0;
                    var effectCount = spellInfo.effects.count;

                    for (; firstEffIndex < effectCount; ++firstEffIndex) {
                        if (((1 << firstEffIndex) & effectMask) != 0) {
                            break;
                        }
                    }

                    if (firstEffIndex >= effectCount) {
                        return;
                    }

                    // get shared data
                    var sharedList = spellInfo.getEffect(firstEffIndex).implicitTargetConditions;

                    // there's already data entry for that sharedMask
                    if (sharedList != null) {
                        // we have overlapping masks in db
                        if (conditionEffMask != effectMask) {
                            Logs.SQL.error("{} in `condition` table, has incorrect SourceGroup {} (spell effectMask) set - " + "effect masks are overlapping (all SourceGroup values having given bit set must be equal) - ignoring.", cond, cond.sourceGroup);

                            return;
                        }
                    }
                    // no data for shared mask, we can create new submask
                    else {
                        // add new list, create new shared mask
                        sharedList = new ArrayList<>();
                        var assigned = false;

                        for (int i = firstEffIndex; i < effectCount; ++i) {
                            if (((1 << i) & commonMask) != 0) {
                                spellInfo.getEffect(i).implicitTargetConditions = sharedList;
                                assigned = true;
                            }
                        }

                        if (!assigned) {
                            break;
                        }
                    }

                    sharedList.add(cond);

                    break;
                }
            }
        });

        return true;
    }

    private boolean addToPhases(Condition cond) {
        if (cond.sourceEntry == 0) {
            var phaseInfo = global.getObjectMgr().getPhaseInfo(cond.sourceGroup);

            if (phaseInfo != null) {
                var found = false;

                for (var areaId : phaseInfo.areas) {
                    var phases = global.getObjectMgr().getPhasesForArea(areaId);

                    if (phases != null) {
                        for (var phase : phases) {
                            if (phase.phaseInfo.id == cond.sourceGroup) {
                                phase.conditions.add(cond);
                                found = true;
                            }
                        }
                    }
                }

                if (found) {
                    return true;
                }
            }
        } else {
            var phases = global.getObjectMgr().getPhasesForArea((int) cond.sourceEntry);

            for (var phase : phases) {
                if (phase.phaseInfo.id == cond.sourceGroup) {
                    phase.conditions.add(cond);

                    return true;
                }
            }
        }

        Logs.SQL.error("{} Area {} does not have phase {}.", cond, cond.sourceGroup, cond.sourceEntry);

        return false;
    }

    private boolean isSourceTypeValid(Condition cond) {
        switch (cond.sourceType) {
            case CREATURE_LOOT_TEMPLATE: {
                if (!LootStorage.CREATURE.haveLootFor(cond.sourceGroup)) {
                    Logs.SQL.error("{} SourceGroup in `condition` table, does not exist in `creature_loot_template`, ignoring.", cond);

                    return false;
                }

                var loot = LootStorage.CREATURE.getLootForConditionFill(cond.sourceGroup);
                var pItemProto = global.getObjectMgr().getItemTemplate((int) cond.sourceEntry);

                if (pItemProto == null && !loot.isReference(cond.sourceEntry)) {
                    Logs.SQL.error("{} sourceType, SourceEntry in `condition` table, Item does not exist, ignoring.", cond);

                    return false;
                }

                break;
            }
            case DISENCHANT_LOOT_TEMPLATE: {
                if (!LootStorage.DISENCHANT.haveLootFor(cond.sourceGroup)) {
                    Logs.SQL.error("{} SourceGroup in `condition` table, does not exist in `disenchant_loot_template`, ignoring.", cond);

                    return false;
                }

                var loot = LootStorage.DISENCHANT.getLootForConditionFill(cond.sourceGroup);
                var pItemProto = global.getObjectMgr().getItemTemplate((int) cond.sourceEntry);

                if (pItemProto == null && !loot.isReference((int) cond.sourceEntry)) {
                    Logs.SQL.error("{} sourceType, SourceEntry in `condition` table, item does not exist, ignoring.", cond);

                    return false;
                }

                break;
            }
            case FISHING_LOOT_TEMPLATE: {
                if (!LootStorage.FISHING.haveLootFor(cond.sourceGroup)) {
                    Logs.SQL.error("{} SourceGroup in `condition` table, does not exist in `fishing_loot_template`, ignoring.", cond);

                    return false;
                }

                var loot = LootStorage.FISHING.getLootForConditionFill(cond.sourceGroup);
                var pItemProto = global.getObjectMgr().getItemTemplate((int) cond.sourceEntry);

                if (pItemProto == null && !loot.isReference((int) cond.sourceEntry)) {
                    Logs.SQL.error("{} sourceType, SourceEntry in `condition` table, item does not exist, ignoring.", cond);

                    return false;
                }

                break;
            }
            case GAME_OBJECT_LOOT_TEMPLATE: {
                if (!LootStorage.GAMEOBJECT.haveLootFor(cond.sourceGroup)) {
                    Logs.SQL.error("{} SourceGroup in `condition` table, does not exist in `gameobject_loot_template`, ignoring.", cond);

                    return false;
                }

                var loot = LootStorage.GAMEOBJECT.getLootForConditionFill(cond.sourceGroup);
                var pItemProto = global.getObjectMgr().getItemTemplate((int) cond.sourceEntry);

                if (pItemProto == null && !loot.isReference((int) cond.sourceEntry)) {
                    Logs.SQL.error("{} sourceType, SourceEntry in `condition` table, item does not exist, ignoring.", cond);

                    return false;
                }

                break;
            }
            case ITEM_LOOT_TEMPLATE: {
                if (!LootStorage.items.haveLootFor(cond.sourceGroup)) {
                    Logs.SQL.error("{} SourceGroup in `condition` table, does not exist in `item_loot_template`, ignoring.", cond);

                    return false;
                }

                var loot = LootStorage.items.getLootForConditionFill(cond.sourceGroup);
                var pItemProto = global.getObjectMgr().getItemTemplate((int) cond.sourceEntry);

                if (pItemProto == null && !loot.isReference((int) cond.sourceEntry)) {
                    Logs.SQL.error("{} sourceType, SourceEntry in `condition` table, item does not exist, ignoring.", cond);

                    return false;
                }

                break;
            }
            case MAIL_LOOT_TEMPLATE: {
                if (!LootStorage.MAIL.haveLootFor(cond.sourceGroup)) {
                    Logs.SQL.error("{} SourceGroup in `condition` table, does not exist in `mail_loot_template`, ignoring.", cond);

                    return false;
                }

                var loot = LootStorage.MAIL.getLootForConditionFill(cond.sourceGroup);
                var pItemProto = global.getObjectMgr().getItemTemplate((int) cond.sourceEntry);

                if (pItemProto == null && !loot.isReference((int) cond.sourceEntry)) {
                    Logs.SQL.error("{} sourceType, SourceEntry in `condition` table, item does not exist, ignoring.", cond);

                    return false;
                }

                break;
            }
            case MILLING_LOOT_TEMPLATE: {
                if (!LootStorage.MILLING.haveLootFor(cond.sourceGroup)) {
                    Logs.SQL.error("{} SourceGroup in `condition` table, does not exist in `milling_loot_template`, ignoring.", cond);

                    return false;
                }

                var loot = LootStorage.MILLING.getLootForConditionFill(cond.sourceGroup);
                var pItemProto = global.getObjectMgr().getItemTemplate((int) cond.sourceEntry);

                if (pItemProto == null && !loot.isReference((int) cond.sourceEntry)) {
                    Logs.SQL.error("{} sourceType, SourceEntry in `condition` table, item does not exist, ignoring.", cond);

                    return false;
                }

                break;
            }
            case PICKPOCKETING_LOOT_TEMPLATE: {
                if (!LootStorage.PICKPOCKETING.haveLootFor(cond.sourceGroup)) {
                    Logs.SQL.error("{} SourceGroup in `condition` table, does not exist in `pickpocketing_loot_template`, ignoring.", cond);

                    return false;
                }

                var loot = LootStorage.PICKPOCKETING.getLootForConditionFill(cond.sourceGroup);
                var pItemProto = global.getObjectMgr().getItemTemplate((int) cond.sourceEntry);

                if (pItemProto == null && !loot.isReference((int) cond.sourceEntry)) {
                    Logs.SQL.error("{} sourceType, SourceEntry in `condition` table, item does not exist, ignoring.", cond);

                    return false;
                }

                break;
            }
            case PROSPECTING_LOOT_TEMPLATE: {
                if (!LootStorage.PROSPECTING.haveLootFor(cond.sourceGroup)) {
                    Logs.SQL.error("{} SourceGroup in `condition` table, does not exist in `prospecting_loot_template`, ignoring.", cond);

                    return false;
                }

                var loot = LootStorage.PROSPECTING.getLootForConditionFill(cond.sourceGroup);
                var pItemProto = global.getObjectMgr().getItemTemplate((int) cond.sourceEntry);

                if (pItemProto == null && !loot.isReference((int) cond.sourceEntry)) {
                    Logs.SQL.error("{} sourceType, SourceEntry in `condition` table, item does not exist, ignoring.", cond);

                    return false;
                }

                break;
            }
            case REFERENCE_LOOT_TEMPLATE: {
                if (!LootStorage.REFERENCE.haveLootFor(cond.sourceGroup)) {
                    Logs.SQL.error("{} SourceGroup in `condition` table, does not exist in `reference_loot_template`, ignoring.", cond);

                    return false;
                }

                var loot = LootStorage.REFERENCE.getLootForConditionFill(cond.sourceGroup);
                var pItemProto = global.getObjectMgr().getItemTemplate((int) cond.sourceEntry);

                if (pItemProto == null && !loot.isReference((int) cond.sourceEntry)) {
                    Logs.SQL.error("{} sourceType, SourceEntry in `condition` table, item does not exist, ignoring.", cond);

                    return false;
                }

                break;
            }
            case SKINNING_LOOT_TEMPLATE: {
                if (!LootStorage.SKINNING.haveLootFor(cond.sourceGroup)) {
                    Logs.SQL.error("{} SourceGroup in `condition` table, does not exist in `skinning_loot_template`, ignoring.", cond);

                    return false;
                }

                var loot = LootStorage.SKINNING.getLootForConditionFill(cond.sourceGroup);
                var pItemProto = global.getObjectMgr().getItemTemplate((int) cond.sourceEntry);

                if (pItemProto == null && !loot.isReference((int) cond.sourceEntry)) {
                    Logs.SQL.error("{} sourceType, SourceEntry in `condition` table, item does not exist, ignoring.", cond);

                    return false;
                }

                break;
            }
            case SpellLootTemplate: {
                if (!LootStorage.spell.haveLootFor(cond.sourceGroup)) {
                    Logs.SQL.error("{} SourceGroup in `condition` table, does not exist in `spell_loot_template`, ignoring.", cond);

                    return false;
                }

                var loot = LootStorage.spell.getLootForConditionFill(cond.sourceGroup);
                var pItemProto = global.getObjectMgr().getItemTemplate((int) cond.sourceEntry);

                if (pItemProto == null && !loot.isReference((int) cond.sourceEntry)) {
                    Logs.SQL.error("{} sourceType, SourceEntry in `condition` table, item does not exist, ignoring.", cond);

                    return false;
                }

                break;
            }
            case SPELL_IMPLICIT_TARGET: {
                var spellInfo = global.getSpellMgr().getSpellInfo((int) cond.sourceEntry, Difficulty.NONE);

                if (spellInfo == null) {
                    Logs.SQL.error("{} SourceEntry in `condition` table does not exist in `spell.db2`, ignoring.", cond);

                    return false;
                }

                if ((cond.sourceGroup > SpellConst.MAX_EFFECT_MASK) || cond.sourceGroup == 0) {
                    Logs.SQL.error("{} in `condition` table, has incorrect sourceGroup (spell effectMask) set, ignoring.", cond);

                    return false;
                }

                var origGroup = cond.sourceGroup;

                for (var spellEffectInfo : spellInfo.getEffects()) {
                    if (((1 << spellEffectInfo.effectIndex) & cond.sourceGroup) == 0) {
                        continue;
                    }

                    if (spellEffectInfo.chainTargets > 0) {
                        continue;
                    }

                    switch (spellEffectInfo.targetA.getSelectionCategory()) {
                        case Nearby:
                        case Cone:
                        case Area:
                        case Traj:
                        case Line:
                            continue;
                        default:
                            break;
                    }

                    switch (spellEffectInfo.targetB.getSelectionCategory()) {
                        case Nearby:
                        case Cone:
                        case Area:
                        case Traj:
                        case Line:
                            continue;
                        default:
                            break;
                    }

                    switch (spellEffectInfo.effect) {
                        case PersistentAreaAura:
                        case ApplyAreaAuraParty:
                        case ApplyAreaAuraRaid:
                        case ApplyAreaAuraFriend:
                        case ApplyAreaAuraEnemy:
                        case ApplyAreaAuraPet:
                        case ApplyAreaAuraOwner:
                        case ApplyAuraOnPet:
                        case ApplyAreaAuraSummons:
                        case ApplyAreaAuraPartyNonrandom:
                            continue;
                        default:
                            break;
                    }

                    Logs.SQL.error("SourceEntry {} SourceGroup {} in `condition` table - spell {} does not have implicit targets of types: _AREA_, _CONE_, _NEARBY_, _CHAIN_ for effect {}, SourceGroup needs correction, ignoring.", cond.sourceEntry, origGroup, cond.sourceEntry, spellEffectInfo.effectIndex);
                    cond.sourceGroup &= ~(1 << spellEffectInfo.effectIndex);
                }

                // all effects were removed, no need to add the condition at all
                if (cond.sourceGroup == 0) {
                    return false;
                }

                break;
            }
            case CREATURE_TEMPLATE_VEHICLE: {
                if (global.getObjectMgr().getCreatureTemplate((int) cond.sourceEntry) == null) {
                    Logs.SQL.error("{} SourceEntry in `condition` table does not exist in `creature_template`, ignoring.", cond);

                    return false;
                }

                break;
            }
            case SPELL:
            case SPELL_PROC: {
                var spellProto = global.getSpellMgr().getSpellInfo((int) cond.sourceEntry, Difficulty.NONE);

                if (spellProto == null) {
                    Logs.SQL.error("{} SourceEntry in `condition` table does not exist in `spell.db2`, ignoring.", cond);

                    return false;
                }

                break;
            }
            case QUEST_AVAILABLE:
                if (global.getObjectMgr().getQuestTemplate((int) cond.sourceEntry) == null) {
                    Logs.SQL.error("{} SourceEntry specifies non-existing quest, skipped.", cond);

                    return false;
                }

                break;
            case VehicleSpell:
                if (global.getObjectMgr().getCreatureTemplate(cond.sourceGroup) == null) {
                    Logs.SQL.error("{} SourceGroup in `condition` table does not exist in `creature_template`, ignoring.", cond);

                    return false;
                }

                if (!global.getSpellMgr().hasSpellInfo((int) cond.sourceEntry, Difficulty.NONE)) {
                    Logs.SQL.error("{} SourceEntry in `condition` table does not exist in `spell.db2`, ignoring.", cond);

                    return false;
                }

                break;
            case SPELL_CLICK_EVENT:
                if (global.getObjectMgr().getCreatureTemplate(cond.sourceGroup) == null) {
                    Logs.SQL.error("{} SourceGroup in `condition` table does not exist in `creature_template`, ignoring.", cond);

                    return false;
                }

                if (!global.getSpellMgr().hasSpellInfo((int) cond.sourceEntry, Difficulty.NONE)) {
                    Logs.SQL.error("{} SourceEntry in `condition` table does not exist in `spell.db2`, ignoring.", cond);

                    return false;
                }

                break;
            case NPC_VENDOR: {
                if (global.getObjectMgr().getCreatureTemplate(cond.sourceGroup) == null) {
                    Logs.SQL.error("{} SourceGroup in `condition` table does not exist in `creature_template`, ignoring.", cond);

                    return false;
                }

                var itemTemplate = global.getObjectMgr().getItemTemplate((int) cond.sourceEntry);

                if (itemTemplate == null) {
                    Logs.SQL.error("{} SourceEntry in `condition` table item does not exist, ignoring.", cond);

                    return false;
                }

                break;
            }
            case TERRAIN_SWAP:
                if (!CliDB.MapStorage.containsKey((int) cond.sourceEntry)) {
                    Logs.SQL.error("{} SourceEntry in `condition` table does not exist in Map.db2, ignoring.", cond);

                    return false;
                }

                break;
            case PHASE:
                if (cond.sourceEntry != 0 && !CliDB.AreaTableStorage.containsKey(cond.sourceEntry)) {
                    Logs.SQL.error("{} SourceEntry in `condition` table does not exist in AreaTable.db2, ignoring.", cond);

                    return false;
                }

                break;
            case GOSSIP_MENU:
            case GOSSIP_MENU_OPTION:
            case SMART_EVENT:
                break;
            case GRAVEYARD:
                if (global.getObjectMgr().getWorldSafeLoc((int) cond.sourceEntry) == null) {
                    Logs.SQL.error(String.format("%1$s SourceEntry in `condition` table, does not exist in WorldSafeLocs.db2, ignoring.", cond));

                    return false;
                }

                break;
            case AREA_TRIGGER:
                if (cond.sourceEntry != 0 && cond.sourceEntry != 1) {
                    Logs.SQL.error(String.format("%1$s in `condition` table, unexpected SourceEntry value (expected 0 or 1), ignoring.", cond));

                    return false;
                }

                if (global.getAreaTriggerDataStorage().GetAreaTriggerTemplate(new areaTriggerId(cond.sourceGroup, cond.sourceEntry != 0)) == null) {
                    Logs.SQL.error(String.format("%1$s in `condition` table, does not exist in `areatrigger_template`, ignoring.", cond));

                    return false;
                }

                break;
            case CONVERSATION_LINE:
                if (global.getConversationDataStorage().GetConversationLineTemplate((int) cond.sourceEntry) == null) {
                    Logs.SQL.error(String.format("%1$s does not exist in `conversation_line_template`, ignoring.", cond));

                    return false;
                }

                break;
            case AREA_TRIGGER_CLIENT_TRIGGERED:
                if (!CliDB.AreaTriggerStorage.containsKey(cond.sourceEntry)) {
                    Logs.SQL.error(String.format("%1$s SourceEntry in `condition` table, does not exists in areaTrigger.db2, ignoring.", cond));

                    return false;
                }

                break;
            case TRAINER_SPELL: {
                if (global.getObjectMgr().getTrainer(cond.sourceGroup) == null) {
                    Logs.SQL.error(String.format("%1$s SourceGroup in `condition` table, does not exist in `trainer`, ignoring.", cond));

                    return false;
                }

                if (global.getSpellMgr().getSpellInfo((int) cond.sourceEntry, Difficulty.NONE) == null) {
                    Logs.SQL.error(String.format("%1$s SourceEntry in `condition` table does not exist in `Spell.db2`, ignoring.", cond));

                    return false;
                }

                break;
            }
            case OBJECT_ID_VISIBILITY: {
                if (cond.sourceGroup <= 0 || cond.sourceGroup >= (int) TypeId.max.getValue()) {
                    Logs.SQL.error(String.format("%1$s SourceGroup in `condition` table, is no valid object type, ignoring.", cond));

                    return false;
                }

                if (cond.sourceGroup == (int) TypeId.UNIT.getValue()) {
                    if (global.getObjectMgr().getCreatureTemplate((int) cond.sourceEntry) == null) {
                        Logs.SQL.error(String.format("%1$s SourceEntry in `condition` table, does not exist in `creature_template`, ignoring.", cond));

                        return false;
                    }
                } else if (cond.sourceGroup == (int) TypeId.gameObject.getValue()) {
                    if (global.getObjectMgr().getGameObjectTemplate((int) cond.sourceEntry) == null) {
                        Logs.SQL.error(String.format("%1$s SourceEntry in `condition` table, does not exist in `gameobject_template`, ignoring.", cond));

                        return false;
                    }
                } else {
                    Logs.SQL.error(String.format("%1$s SourceGroup in `condition` table, uses unchecked type id, ignoring.", cond));

                    return false;
                }

                break;
            }
            case SpawnGroup: {
                var spawnGroup = global.getObjectMgr().getSpawnGroupData((int) cond.sourceEntry);

                if (spawnGroup == null) {
                    Logs.SQL.error(String.format("%1$s SourceEntry in `condition` table, does not exist in `spawn_group_template`, ignoring.", cond));

                    return false;
                }

                if (spawnGroup.getFlags().hasFlag(SpawnGroupFlags.System)) {
                    Logs.SQL.error(String.format("%1$s in `spawn_group_template` table cannot have SPAWNGROUP_FLAG_SYSTEM or SPAWNGROUP_FLAG_MANUAL_SPAWN flags, ignoring.", cond));

                    return false;
                }

                break;
            }
            default:
                Logs.SQL.error(String.format("%1$s Invalid ConditionSourceType in `condition` table, ignoring.", cond));

                return false;
        }

        return true;
    }

    private boolean isConditionTypeValid(Condition cond) {
        switch (cond.conditionType) {
            case AURA: {
                if (!global.getSpellMgr().hasSpellInfo(cond.conditionValue1, Difficulty.NONE)) {
                    Logs.SQL.error("{} has non existing spell (Id: {}), skipped", cond, cond.conditionValue1);

                    return false;
                }

                break;
            }
            case ITEM: {
                var proto = global.getObjectMgr().getItemTemplate(cond.conditionValue1);

                if (proto == null) {
                    Logs.SQL.error("{} item ({}) does not exist, skipped", cond, cond.conditionValue1);

                    return false;
                }

                if (cond.conditionValue2 == 0) {
                    Logs.SQL.error("{} Zero item count in conditionValue2, skipped", cond);

                    return false;
                }

                break;
            }
            case ITEM_EQUIPPED: {
                var proto = global.getObjectMgr().getItemTemplate(cond.conditionValue1);

                if (proto == null) {
                    Logs.SQL.error("{} item ({}) does not exist, skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            }
            case ZONEID: {
                var areaEntry = CliDB.AreaTableStorage.get(cond.conditionValue1);

                if (areaEntry == null) {
                    Logs.SQL.error("{} area ({}) does not exist, skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                if (areaEntry.ParentAreaID != 0) {
                    Logs.SQL.error("{} requires to be in area ({}) which is a subzone but zone expected, skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            }
            case REPUTATION_RANK: {
                if (!CliDB.FactionStorage.containsKey(cond.conditionValue1)) {
                    Logs.SQL.error("{} has non existing faction ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            }
            case Team: {
                if (cond.conditionValue1 != (int) Team.ALLIANCE.getValue() && cond.conditionValue1 != (int) Team.Horde.getValue()) {
                    Logs.SQL.error("{} specifies unknown team ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            }
            case SKILL: {
                var pSkill = CliDB.SkillLineStorage.get(cond.conditionValue1);

                if (pSkill == null) {
                    Logs.SQL.error("{} specifies non-existing skill ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                if (cond.conditionValue2 < 1 || cond.conditionValue2 > global.getWorldMgr().getConfigMaxSkillValue()) {
                    Logs.SQL.error("{} specifies skill ({}) with invalid value ({}), skipped.", cond.toString(true), cond.conditionValue1, cond.conditionValue2);

                    return false;
                }

                break;
            }
            case QUEST_STATE:
                if (cond.conditionValue2 >= (1 << QuestStatus.max.getValue())) {
                    Logs.SQL.error("{} has invalid state mask ({}), skipped.", cond.toString(true), cond.conditionValue2);

                    return false;
                }

                if (global.getObjectMgr().getQuestTemplate(cond.conditionValue1) == null) {
                    Logs.SQL.error("{} points to non-existing quest ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            case QUEST_REWARDED:
            case QUEST_TAKEN:
            case QUEST_NONE:
            case QUEST_COMPLETE:
            case DAILY_QUEST_DONE: {
                if (global.getObjectMgr().getQuestTemplate(cond.conditionValue1) == null) {
                    Logs.SQL.error("{} points to non-existing quest ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            }
            case ACTIVE_EVENT: {
                var events = global.getGameEventMgr().getEventMap();

                if (cond.conditionValue1 >= events.length || !events[cond.ConditionValue1].isValid()) {
                    Logs.SQL.error("{} has non existing event id ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            }
            case ACHIEVEMENT: {
                var achievement = CliDB.AchievementStorage.get(cond.conditionValue1);

                if (achievement == null) {
                    Logs.SQL.error("{} has non existing achivement id ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            }
            case CLASS: {
                if ((boolean) (cond.conditionValue1 & ~(int) playerClass.ClassMaskAllPlayable.getValue())) {
                    Logs.SQL.error("{} has non existing classmask ({}), skipped.", cond.toString(true), cond.conditionValue1 & ~(int) playerClass.ClassMaskAllPlayable.getValue());

                    return false;
                }

                break;
            }
            case RACE: {
                if ((boolean) (cond.conditionValue1 & ~SharedConst.RaceMaskAllPlayable)) {
                    Logs.SQL.error("{} has non existing racemask ({}), skipped.", cond.toString(true), cond.conditionValue1 & ~SharedConst.RaceMaskAllPlayable);

                    return false;
                }

                break;
            }
            case GENDER: {
                if (!player.isValidGender(gender.forValue((byte) cond.conditionValue1))) {
                    Logs.SQL.error("{} has invalid gender ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            }
            case MAP_ID: {
                var me = CliDB.MapStorage.get(cond.conditionValue1);

                if (me == null) {
                    Logs.SQL.error("{} has non existing map ({}), skipped", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            }
            case SPELL: {
                if (!global.getSpellMgr().hasSpellInfo(cond.conditionValue1, Difficulty.NONE)) {
                    Logs.SQL.error("{} has non existing spell (Id: {}), skipped", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            }
            case LEVEL: {
                if (cond.conditionValue2 >= (int) ComparisionType.max.getValue()) {
                    Logs.SQL.error("{} has invalid ComparisionType ({}), skipped.", cond.toString(true), cond.conditionValue2);

                    return false;
                }

                break;
            }
            case DRUNKEN_STATE: {
                if (cond.conditionValue1 > (int) DrunkenState.Smashed.getValue()) {
                    Logs.SQL.error("{} has invalid state ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            }
            case NEAR_CREATURE: {
                if (global.getObjectMgr().getCreatureTemplate(cond.conditionValue1) == null) {
                    Logs.SQL.error("{} has non existing creature template entry ({}), skipped", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            }
            case NearGameobject: {
                if (global.getObjectMgr().getGameObjectTemplate(cond.conditionValue1) == null) {
                    Logs.SQL.error("{} has non existing gameobject template entry ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            }
            case OBJECT_ENTRY_GUID: {
                switch (TypeId.values()[cond.conditionValue1]) {
                    case UNIT:
                        if (cond.conditionValue2 != 0 && global.getObjectMgr().getCreatureTemplate(cond.conditionValue2) == null) {
                            Logs.SQL.error("{} has non existing creature template entry ({}), skipped.", cond.toString(true), cond.conditionValue2);

                            return false;
                        }

                        if (cond.conditionValue3 != 0) {
                            var creatureData = global.getObjectMgr().getCreatureData(cond.conditionValue3);

                            if (creatureData != null) {
                                if (cond.conditionValue2 != 0 && creatureData.id != cond.conditionValue2) {
                                    Logs.SQL.error("{} has guid {} set but does not match creature entry ({}), skipped.", cond.toString(true), cond.conditionValue3, cond.conditionValue2);

                                    return false;
                                }
                            } else {
                                Logs.SQL.error("{} has non existing creature guid ({}), skipped.", cond.toString(true), cond.conditionValue3);

                                return false;
                            }
                        }

                        break;
                    case GAME_OBJECT:
                        if (cond.conditionValue2 != 0 && global.getObjectMgr().getGameObjectTemplate(cond.conditionValue2) == null) {
                            Logs.SQL.error("{} has non existing gameobject template entry ({}), skipped.", cond.toString(true), cond.conditionValue2);

                            return false;
                        }

                        if (cond.conditionValue3 != 0) {
                            var goData = global.getObjectMgr().getGameObjectData(cond.conditionValue3);

                            if (goData != null) {
                                if (cond.conditionValue2 != 0 && goData.id != cond.conditionValue2) {
                                    Logs.SQL.error("{} has guid {} set but does not match gameobject entry ({}), skipped.", cond.toString(true), cond.conditionValue3, cond.conditionValue2);

                                    return false;
                                }
                            } else {
                                Logs.SQL.error("{} has non existing gameobject guid ({}), skipped.", cond.toString(true), cond.conditionValue3);

                                return false;
                            }
                        }

                        break;
                    case PLAYER:
                    case CORPSE:
                        if (cond.conditionValue2 != 0) {
                            logUselessConditionValue(cond, (byte) 2, cond.conditionValue2);
                        }

                        if (cond.conditionValue3 != 0) {
                            logUselessConditionValue(cond, (byte) 3, cond.conditionValue3);
                        }

                        break;
                    default:
                        Logs.SQL.error("{} has wrong typeid set ({}), skipped", cond.toString(true), cond.conditionValue1);

                        return false;
                }

                break;
            }
            case TYPE_MASK: {
                if (cond.conditionValue1 == 0 || (boolean) (cond.conditionValue1 & ~(int) (TypeMask.unit.getValue() | TypeMask.player.getValue() | TypeMask.gameObject.getValue() | TypeMask.Corpse.getValue()))) {
                    Logs.SQL.error("{} has invalid typemask set ({}), skipped.", cond.toString(true), cond.conditionValue2);

                    return false;
                }

                break;
            }
            case RELATION_TO: {
                if (cond.conditionValue1 >= cond.getMaxAvailableConditionTargets()) {
                    Logs.SQL.error("{} has invalid conditionValue1(ConditionTarget selection) ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                if (cond.conditionValue1 == cond.conditionTarget) {
                    Logs.SQL.error("{} has conditionValue1(ConditionTarget selection) set to self ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                if (cond.conditionValue2 >= (int) RelationType.max.getValue()) {
                    Logs.SQL.error("{} has invalid conditionValue2(RelationType) ({}), skipped.", cond.toString(true), cond.conditionValue2);

                    return false;
                }

                break;
            }
            case REACTION_TO: {
                if (cond.conditionValue1 >= cond.getMaxAvailableConditionTargets()) {
                    Logs.SQL.error("{} has invalid conditionValue1(ConditionTarget selection) ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                if (cond.conditionValue1 == cond.conditionTarget) {
                    Logs.SQL.error("{} has conditionValue1(ConditionTarget selection) set to self ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                if (cond.conditionValue2 == 0) {
                    Logs.SQL.error("{} has invalid conditionValue2(rankMask) ({}), skipped.", cond.toString(true), cond.conditionValue2);

                    return false;
                }

                break;
            }
            case DISTANCE_TO: {
                if (cond.conditionValue1 >= cond.getMaxAvailableConditionTargets()) {
                    Logs.SQL.error("{} has invalid conditionValue1(ConditionTarget selection) ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                if (cond.conditionValue1 == cond.conditionTarget) {
                    Logs.SQL.error("{} has conditionValue1(ConditionTarget selection) set to self ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                if (cond.conditionValue3 >= (int) ComparisionType.max.getValue()) {
                    Logs.SQL.error("{} has invalid ComparisionType ({}), skipped.", cond.toString(true), cond.conditionValue3);

                    return false;
                }

                break;
            }
            case HP_VAL: {
                if (cond.conditionValue2 >= (int) ComparisionType.max.getValue()) {
                    Logs.SQL.error("{} has invalid ComparisionType ({}), skipped.", cond.toString(true), cond.conditionValue2);

                    return false;
                }

                break;
            }
            case HP_PCT: {
                if (cond.conditionValue1 > 100) {
                    Logs.SQL.error("{} has too big percent value ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                if (cond.conditionValue2 >= (int) ComparisionType.max.getValue()) {
                    Logs.SQL.error("{} has invalid ComparisionType ({}), skipped.", cond.toString(true), cond.conditionValue2);

                    return false;
                }

                break;
            }
            case WORLD_STATE: {
                if (global.getWorldStateMgr().getWorldStateTemplate((int) cond.conditionValue1) == null) {
                    Logs.SQL.error("{} has non existing world state in value1 ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            }
            case PHASE_ID: {
                if (!CliDB.PhaseStorage.containsKey(cond.conditionValue1)) {
                    Logs.SQL.error("{} has nonexistent phaseid in value1 ({}), skipped", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            }
            case TITLE: {
                var titleEntry = CliDB.CharTitlesStorage.get(cond.conditionValue1);

                if (titleEntry == null) {
                    Logs.SQL.error("{} has non existing title in value1 ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            }
            case SpawnmaskDeprecated: {
                Logs.SQL.error(String.format("%1$s using deprecated condition type CONDITION_SPAWNMASK.", cond.toString(true)));

                return false;
            }
            case UNIT_STATE: {
                if (cond.conditionValue1 > (int) UnitState.AllStateSupported.getValue()) {
                    Logs.SQL.error("{} has non existing UnitState in value1 ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            }
            case CREATURE_TYPE: {
                if (cond.conditionValue1 == 0 || cond.conditionValue1 > (int) creatureType.GasCloud.getValue()) {
                    Logs.SQL.error("{} has non existing CreatureType in value1 ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            }
            case REALM_ACHIEVEMENT: {
                var achievement = CliDB.AchievementStorage.get(cond.conditionValue1);

                if (achievement == null) {
                    Logs.SQL.error("{} has non existing realm first achivement id ({}), skipped.", cond, cond.conditionValue1);

                    return false;
                }

                break;
            }
            case STAND_STATE: {
                boolean valid;

                switch (cond.conditionValue1) {
                    case 0:
                        valid = cond.conditionValue2 <= (int) UnitStandStateType.Submerged.getValue();

                        break;
                    case 1:
                        valid = cond.conditionValue2 <= 1;

                        break;
                    default:
                        valid = false;

                        break;
                }

                if (!valid) {
                    Logs.SQL.error("{} has non-existing stand state ({},{}), skipped.", cond.toString(true), cond.conditionValue1, cond.conditionValue2);

                    return false;
                }

                break;
            }
            case ObjectiveProgress: {
                var obj = global.getObjectMgr().getQuestObjective(cond.conditionValue1);

                if (obj == null) {
                    Logs.SQL.error("{} points to non-existing quest objective ({}), skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                var limit = obj.isStoringFlag() ? 1 : obj.amount;

                if (cond.conditionValue3 > limit) {
                    Logs.SQL.error(String.format("%1$s has quest objective count %2$s in value3, but quest objective %3$s has a maximum objective count of %4$s, skipped.", cond.toString(true), cond.conditionValue3, cond.conditionValue1, limit));

                    return false;
                }

                break;
            }
            case PetType:
                if (cond.conditionValue1 >= (1 << PetType.max.getValue())) {
                    Logs.SQL.error("{} has non-existing pet type {}, skipped.", cond.toString(true), cond.conditionValue1);

                    return false;
                }

                break;
            case Alive:
            case Areaid:
            case InstanceInfo:
            case TerrainSwap:
            case InWater:
            case Charmed:
            case Taxi:
            case Gamemaster:
                break;
            case DifficultyId:
                if (!CliDB.DifficultyStorage.containsKey(cond.conditionValue1)) {
                    Logs.SQL.error(String.format("%1$s has non existing difficulty in value1 (%2$s), skipped.", cond.toString(true), cond.conditionValue1));

                    return false;
                }

                break;
            case BattlePetCount:
                if (!CliDB.BattlePetSpeciesStorage.containsKey(cond.conditionValue1)) {
                    Logs.SQL.error(String.format("%1$s has non existing BattlePet SpeciesId in value1 (%2$s), skipped.", cond.toString(true), cond.conditionValue1));

                    return false;
                }

                if (cond.conditionValue2 > SharedConst.DefaultMaxBattlePetsPerSpecies) {
                    Logs.SQL.error(String.format("%1$s has invalid (greater than %2$s) value2 (%3$s), skipped.", cond.toString(true), SharedConst.DefaultMaxBattlePetsPerSpecies, cond.conditionValue2));

                    return false;
                }

                if (cond.conditionValue3 >= (int) ComparisionType.max.getValue()) {
                    Logs.SQL.error(String.format("%1$s has invalid ComparisionType (%2$s), skipped.", cond.toString(true), cond.conditionValue3));

                    return false;
                }

                break;
            case ScenarioStep: {
                if (!CliDB.ScenarioStepStorage.containsKey(cond.conditionValue1)) {
                    Logs.SQL.error(String.format("%1$s has non existing ScenarioStep in value1 (%2$s), skipped.", cond.toString(true), cond.conditionValue1));

                    return false;
                }

                break;
            }
            case SceneInProgress: {
                if (!CliDB.SceneScriptPackageStorage.containsKey(cond.conditionValue1)) {
                    Logs.SQL.error(String.format("%1$s has non existing SceneScriptPackageId in value1 (%2$s), skipped.", cond.toString(true), cond.conditionValue1));

                    return false;
                }

                break;
            }
            case PlayerCondition: {
                if (!CliDB.PlayerConditionStorage.containsKey(cond.conditionValue1)) {
                    Logs.SQL.error(String.format("%1$s has non existing PlayerConditionId in value1 (%2$s), skipped.", cond.toString(true), cond.conditionValue1));

                    return false;
                }

                break;
            }
            default:
                Logs.SQL.error(String.format("%1$s Invalid ConditionType in `condition` table, ignoring.", cond));

                return false;
        }

        if (cond.conditionTarget >= cond.getMaxAvailableConditionTargets()) {
            Logs.SQL.error(String.format("%1$s in `condition` table, has incorrect ConditionTarget set, ignoring.", cond.toString(true)));

            return false;
        }

        if (cond.conditionValue1 != 0 && !StaticConditionTypeData[cond.conditionType.getValue()].hasConditionValue1) {
            logUselessConditionValue(cond, (byte) 1, cond.conditionValue1);
        }

        if (cond.conditionValue2 != 0 && !StaticConditionTypeData[cond.conditionType.getValue()].hasConditionValue2) {
            logUselessConditionValue(cond, (byte) 2, cond.conditionValue2);
        }

        if (cond.conditionValue3 != 0 && !StaticConditionTypeData[cond.conditionType.getValue()].hasConditionValue3) {
            logUselessConditionValue(cond, (byte) 3, cond.conditionValue3);
        }

        return true;
    }

    private void logUselessConditionValue(Condition cond, byte index, int value) {
        Logs.SQL.error("{} has useless data in ConditionValue{} ({})!", cond.toString(true), index, value);
    }

    private void clean() {
        conditionReferenceStorage.clear();

        conditionStorage.clear();

        for (ConditionSourceType i = 0; i.getValue() < ConditionSourceType.max.getValue(); ++i) {
            conditionStorage.put(i, new MultiMap<Integer, condition>()); //add new empty list for SourceType
        }

        vehicleSpellConditionStorage.clear();

        smartEventConditionStorage.clear();

        spellClickEventConditionStorage.clear();
        spellsUsedInSpellClickConditions.clear();

        npcVendorConditionContainerStorage.clear();

        areaTriggerConditionContainerStorage.clear();

        trainerSpellConditionContainerStorage.clear();

        objectVisibilityConditionStorage.clear();
    }

}
