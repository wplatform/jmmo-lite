package com.github.azeroth.game.condition;

import com.github.azeroth.common.Assert;
import com.github.azeroth.game.domain.condition.Condition;
import com.github.azeroth.game.domain.condition.InstanceInfo;
import com.github.azeroth.game.domain.instance.EncounterState;
import com.github.azeroth.game.map.BattlegroundMap;
import com.github.azeroth.game.map.InstanceMap;
import com.github.azeroth.game.map.InstanceScript;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


import java.util.function.Predicate;

import static com.github.azeroth.game.condition.ConditionSourceInfo.MAX_CONDITION_TARGETS;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Conditions {

    public static Predicate<ConditionSourceInfo> condition(Condition condition) {
        return sourceInfo -> {

            Assert.state(condition.conditionTarget < MAX_CONDITION_TARGETS);

            var map = sourceInfo.mConditionMap;
            boolean condMeets = false;
            boolean needsObject = false;
            switch (condition.conditionType) {
                case NONE:
                    condMeets = true;                                    // empty condition, always met
                    break;
                case ACTIVE_EVENT:
                    condMeets = GameEventMgr.isActiveEvent(condition.conditionValue1);
                    break;
                case INSTANCE_INFO: {
                    if (map instanceof InstanceMap instanceMap) {
                        var instance = instanceMap.getInstanceScript();
                        if (instance != null)
                        {
                            condMeets = switch (condition.conditionValue3) {
                                case InstanceInfo.DATA ->
                                        instance.getData(condition.conditionValue1) == condition.conditionValue2;
                                case InstanceInfo.BOSS_STATE ->
                                        instance.getBossState(condition.conditionValue1) == EncounterState.values()[condition.conditionValue2];
                                case InstanceInfo.DATA64 ->
                                        instance.getData64(condition.conditionValue1) == condition.conditionValue2;
                                default -> false;
                            };
                        }
                    } else if (map instanceof BattlegroundMap bgMap)
                    {
                        var zoneScript = bgMap.getBattlegroundScript();
                        condMeets = switch (condition.conditionValue3) {
                            case InstanceInfo.DATA ->
                                    zoneScript.getData(condition.conditionValue1) == condition.conditionValue2;
                            case InstanceInfo.DATA64 ->
                                    zoneScript.getData64(condition.conditionValue1) == condition.conditionValue2;
                            default -> false;
                        };
                    }
                    break;
                }
                case MAPID:
                    condMeets = map.getId() == condition.conditionValue1;
                    break;
                case WORLD_STATE: {
                    condMeets = sWorldStateMgr.getValue(condition.conditionValue1, map) == int32(condition.conditionValue2);
                    break;
                }
                case REALM_ACHIEVEMENT: {
                    AchievementEntry const*achievement = sAchievementStore.LookupEntry(ConditionValue1);
                    if (achievement && sAchievementMgr -> IsRealmCompleted(achievement))
                        condMeets = true;
                    break;
                }
                case DIFFICULTY_ID: {
                    condMeets = map -> GetDifficultyID() == ConditionValue1;
                    break;
                }
                case SCENARIO_STEP: {
                    if (InstanceMap const*instanceMap = map -> ToInstanceMap())
                    if (Scenario const*scenario = instanceMap -> GetInstanceScenario())
                    if (ScenarioStepEntry const*step = scenario -> GetStep())
                    condMeets = step -> ID == ConditionValue1;
                    break;
                }
                default:
                    needsObject = true;
                    break;
            }

            WorldObject const*object = sourceInfo.mConditionTargets[ConditionTarget];
            // object not present, return false
            if (needsObject && !object) {
                TC_LOG_DEBUG("condition", "Condition object not found for {}", ToString());
                return false;
            }
            switch (ConditionType) {
                case AURA: {
                    if (Unit const*unit = object -> ToUnit())
                    condMeets = unit -> HasAuraEffect(ConditionValue1, ConditionValue2);
                    break;
                }
                case ITEM: {
                    if (Player const*player = object -> ToPlayer())
                    {
                        // don't allow 0 items (it's checked during table load)
                        ASSERT(ConditionValue2);
                        bool checkBank = ConditionValue3 ? true : false;
                        condMeets = player -> HasItemCount(ConditionValue1, ConditionValue2, checkBank);
                    }
                    break;
                }
                case ITEM_EQUIPPED: {
                    if (Player const*player = object -> ToPlayer())
                    condMeets = player -> HasItemOrGemWithIdEquipped(ConditionValue1, 1);
                    break;
                }
                case ZONEID:
                    condMeets = object -> GetZoneId() == ConditionValue1;
                    break;
                case REPUTATION_RANK: {
                    if (Player const*player = object -> ToPlayer())
                    {
                        if (FactionEntry const*faction = sFactionStore.LookupEntry(ConditionValue1))
                        condMeets = (ConditionValue2 & (1 << player -> GetReputationMgr().GetRank(faction))) != 0;
                    }
                    break;
                }
                case ACHIEVEMENT: {
                    if (Player const*player = object -> ToPlayer())
                    condMeets = player -> HasAchieved(ConditionValue1);
                    break;
                }
                case TEAM: {
                    if (Player const*player = object -> ToPlayer())
                    condMeets = player -> GetTeam() == Team(ConditionValue1);
                    break;
                }
                case CLASS: {
                    if (Unit const*unit = object -> ToUnit())
                    condMeets = (unit -> GetClassMask() & ConditionValue1) != 0;
                    break;
                }
                case RACE: {
                    if (Unit const*unit = object -> ToUnit())
                    condMeets = Trinity::RaceMask < uint32 > {ConditionValue1}.HasRace(unit -> GetRace());
                    break;
                }
                case GENDER: {
                    if (Player const*player = object -> ToPlayer())
                    condMeets = player -> GetNativeGender() == Gender(ConditionValue1);
                    break;
                }
                case SKILL: {
                    if (Player const*player = object -> ToPlayer())
                    condMeets = player -> HasSkill(ConditionValue1) && player -> GetBaseSkillValue(ConditionValue1) >= ConditionValue2;
                    break;
                }
                case QUESTREWARDED: {
                    if (Player const*player = object -> ToPlayer())
                    condMeets = player -> GetQuestRewardStatus(ConditionValue1);
                    break;
                }
                case QUESTTAKEN: {
                    if (Player const*player = object -> ToPlayer())
                    {
                        QuestStatus status = player -> GetQuestStatus(ConditionValue1);
                        condMeets = (status == QUEST_STATUS_INCOMPLETE);
                    }
                    break;
                }
                case QUEST_COMPLETE: {
                    if (Player const*player = object -> ToPlayer())
                    {
                        QuestStatus status = player -> GetQuestStatus(ConditionValue1);
                        condMeets = (status == QUEST_STATUS_COMPLETE && !player -> GetQuestRewardStatus(ConditionValue1));
                    }
                    break;
                }
                case QUEST_NONE: {
                    if (Player const*player = object -> ToPlayer())
                    {
                        QuestStatus status = player -> GetQuestStatus(ConditionValue1);
                        condMeets = (status == QUEST_STATUS_NONE);
                    }
                    break;
                }
                case AREAID:
                    condMeets = DB2Manager::IsInArea (object -> GetAreaId(), ConditionValue1);
                    break;
                case SPELL: {
                    if (Player const*player = object -> ToPlayer())
                    condMeets = player -> HasSpell(ConditionValue1);
                    break;
                }
                case LEVEL: {
                    if (Unit const*unit = object -> ToUnit())
                    condMeets = CompareValues(static_cast < ComparisionType > (ConditionValue2), static_cast < uint32 > (unit -> GetLevel()), ConditionValue1);
                    break;
                }
                case DRUNKENSTATE: {
                    if (Player const*player = object -> ToPlayer())
                    condMeets = (uint32) Player::GetDrunkenstateByValue (player -> GetDrunkValue()) >= ConditionValue1;
                    break;
                }
                case NEAR_CREATURE: {
                    condMeets = object -> FindNearestCreature(ConditionValue1, (float) ConditionValue2, bool(!ConditionValue3)) != nullptr;
                    break;
                }
                case NEAR_GAMEOBJECT: {
                    condMeets = object -> FindNearestGameObject(ConditionValue1, (float) ConditionValue2) != nullptr;
                    break;
                }
                case OBJECT_ENTRY_GUID: {
                    if (uint32(object -> GetTypeId()) == ConditionValue1) {
                        condMeets = !ConditionValue2 || (object -> GetEntry() == ConditionValue2);

                        if (ConditionValue3) {
                            switch (object -> GetTypeId()) {
                                case TYPEID_UNIT:
                                    condMeets &= object -> ToCreature()->GetSpawnId() == ConditionValue3;
                                    break;
                                case TYPEID_GAMEOBJECT:
                                    condMeets &= object -> ToGameObject()->GetSpawnId() == ConditionValue3;
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    break;
                }
                case TYPE_MASK: {
                    condMeets = object -> isType(ConditionValue1);
                    break;
                }
                case RELATION_TO: {
                    if (WorldObject const*toObject = sourceInfo.mConditionTargets[ConditionValue1])
                    {
                        Unit const*toUnit = toObject -> ToUnit();
                        Unit const*unit = object -> ToUnit();
                        if (toUnit && unit) {
                            switch (static_cast < RelationType > (ConditionValue2)) {
                                case RELATION_SELF:
                                    condMeets = unit == toUnit;
                                    break;
                                case RELATION_IN_PARTY:
                                    condMeets = unit -> IsInPartyWith(toUnit);
                                    break;
                                case RELATION_IN_RAID_OR_PARTY:
                                    condMeets = unit -> IsInRaidWith(toUnit);
                                    break;
                                case RELATION_OWNED_BY:
                                    condMeets = unit -> GetOwnerGUID() == toUnit -> GetGUID();
                                    break;
                                case RELATION_PASSENGER_OF:
                                    condMeets = unit -> IsOnVehicle(toUnit);
                                    break;
                                case RELATION_CREATED_BY:
                                    condMeets = unit -> GetCreatorGUID() == toUnit -> GetGUID();
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    break;
                }
                case REACTION_TO: {
                    if (WorldObject const*toObject = sourceInfo.mConditionTargets[ConditionValue1])
                    {
                        Unit const*toUnit = toObject -> ToUnit();
                        Unit const*unit = object -> ToUnit();
                        if (toUnit && unit)
                            condMeets = ((1 << unit -> GetReactionTo(toUnit)) & ConditionValue2) != 0;
                    }
                    break;
                }
                case DISTANCE_TO: {
                    if (WorldObject const*toObject = sourceInfo.mConditionTargets[ConditionValue1])
                    condMeets = CompareValues(static_cast < ComparisionType > (ConditionValue3), object -> GetDistance(toObject), static_cast <
                    float>(ConditionValue2));
                    break;
                }
                case ALIVE: {
                    if (Unit const*unit = object -> ToUnit())
                    condMeets = unit -> IsAlive();
                    break;
                }
                case HP_VAL: {
                    if (Unit const*unit = object -> ToUnit())
                    condMeets = CompareValues(static_cast < ComparisionType > (ConditionValue2), unit -> GetHealth(), static_cast < uint64 > (ConditionValue1));
                    break;
                }
                case HP_PCT: {
                    if (Unit const*unit = object -> ToUnit())
                    condMeets = CompareValues(static_cast < ComparisionType > (ConditionValue2), unit -> GetHealthPct(), static_cast <
                    float>(ConditionValue1));
                    break;
                }
                case PHASEID: {
                    condMeets = object -> GetPhaseShift().HasPhase(ConditionValue1);
                    break;
                }
                case TITLE: {
                    if (Player const*player = object -> ToPlayer())
                    condMeets = player -> HasTitle(ConditionValue1);
                    break;
                }
                case UNIT_STATE: {
                    if (Unit const*unit = object -> ToUnit())
                    condMeets = unit -> HasUnitState(ConditionValue1);
                    break;
                }
                case CREATURE_TYPE: {
                    if (Creature const*creature = object -> ToCreature())
                    condMeets = creature -> GetCreatureTemplate()->type == ConditionValue1;
                    break;
                }
                case IN_WATER: {
                    if (Unit const*unit = object -> ToUnit())
                    condMeets = unit -> IsInWater();
                    break;
                }
                case TERRAIN_SWAP: {
                    condMeets = object -> GetPhaseShift().HasVisibleMapId(ConditionValue1);
                    break;
                }
                case STAND_STATE: {
                    if (Unit const*unit = object -> ToUnit())
                    {
                        if (ConditionValue1 == 0)
                            condMeets = (unit -> GetStandState() == UnitStandStateType(ConditionValue2));
                        else if (ConditionValue2 == 0)
                            condMeets = unit -> IsStandState();
                        else if (ConditionValue2 == 1)
                            condMeets = unit -> IsSitState();
                    }
                    break;
                }
                case DAILY_QUEST_DONE: {
                    if (Player const*player = object -> ToPlayer())
                    condMeets = player -> IsDailyQuestDone(ConditionValue1);
                    break;
                }
                case CHARMED: {
                    if (Unit const*unit = object -> ToUnit())
                    condMeets = unit -> IsCharmed();
                    break;
                }
                case PET_TYPE: {
                    if (Player const*player = object -> ToPlayer())
                    if (Pet const*pet = player -> GetPet())
                    condMeets = (((1 << pet -> getPetType()) & ConditionValue1) != 0);
                    break;
                }
                case TAXI: {
                    if (Player const*player = object -> ToPlayer())
                    condMeets = player -> IsInFlight();
                    break;
                }
                case QUESTSTATE: {
                    if (Player const*player = object -> ToPlayer())
                    {
                        if (
                                ((ConditionValue2 & (1 << QUEST_STATUS_NONE)) && (player -> GetQuestStatus(ConditionValue1) == QUEST_STATUS_NONE)) ||
                                        ((ConditionValue2 & (1 << QUEST_STATUS_COMPLETE)) && (player -> GetQuestStatus(ConditionValue1) == QUEST_STATUS_COMPLETE)) ||
                                        ((ConditionValue2 & (1 << QUEST_STATUS_INCOMPLETE)) && (player -> GetQuestStatus(ConditionValue1) == QUEST_STATUS_INCOMPLETE)) ||
                                        ((ConditionValue2 & (1 << QUEST_STATUS_FAILED)) && (player -> GetQuestStatus(ConditionValue1) == QUEST_STATUS_FAILED)) ||
                                        ((ConditionValue2 & (1 << QUEST_STATUS_REWARDED)) && player -> GetQuestRewardStatus(ConditionValue1))
                        )
                            condMeets = true;
                    }
                    break;
                }
                case QUEST_OBJECTIVE_PROGRESS: {
                    if (Player const*player = object -> ToPlayer())
                    {
                        QuestObjective const*obj = sObjectMgr -> GetQuestObjective(ConditionValue1);
                        if (!obj)
                            break;

                        Quest const*quest = sObjectMgr -> GetQuestTemplate(obj -> QuestID);
                        if (!quest)
                            break;

                        uint16 slot = player -> FindQuestSlot(obj -> QuestID);
                        if (slot >= MAX_QUEST_LOG_SIZE)
                            break;

                        condMeets = player -> GetQuestSlotObjectiveData(slot, * obj) ==int32(ConditionValue3);
                    }
                    break;
                }
                case GAMEMASTER: {
                    if (Player const*player = object -> ToPlayer())
                    {
                        if (ConditionValue1 == 1)
                            condMeets = player -> CanBeGameMaster();
                        else
                            condMeets = player -> IsGameMaster();
                    }
                    break;
                }
                case BATTLE_PET_COUNT: {
                    if (Player const*player = object -> ToPlayer())
                    condMeets = CompareValues(static_cast < ComparisionType > (ConditionValue3),
                            player -> GetSession()->GetBattlePetMgr()->
                    GetPetCount(sBattlePetSpeciesStore.AssertEntry(ConditionValue1), player -> GetGUID()),
                            static_cast < uint8 > (ConditionValue2));
                    break;
                }
                case SCENE_IN_PROGRESS: {
                    if (Player const*player = object -> ToPlayer())
                    condMeets = player -> GetSceneMgr().GetActiveSceneCount(ConditionValue1) > 0;
                    break;
                }
                case PLAYER_CONDITION: {
                    if (Player const*player = object -> ToPlayer())
                    condMeets = ConditionMgr::IsPlayerMeetingCondition (player, ConditionValue1);
                    break;
                }
                case PRIVATE_OBJECT: {
                    condMeets = !object -> GetPrivateObjectOwner().IsEmpty();
                    break;
                }
                case STRING_ID: {
                    if (Creature const*creature = object -> ToCreature())
                    condMeets = creature -> HasStringId(ConditionStringValue1);
            else if (GameObject const*go = object -> ToGameObject())
                    condMeets = go -> HasStringId(ConditionStringValue1);
                    break;
                }
                default:
                    break;
            }

            if (NegativeCondition)
                condMeets = !condMeets;

            if (!condMeets)
                sourceInfo.mLastFailedCondition = this;

            return condMeets && sScriptMgr -> OnConditionCheck(this, sourceInfo); // Returns true by default.;
            return true;
        };
    }
}
