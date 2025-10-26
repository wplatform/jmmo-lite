package com.github.azeroth.game.achievement;


import com.github.azeroth.dbc.defines.CriteriaType;
import com.github.azeroth.game.domain.condition.DisableType;

import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;


public class CriteriaHandler {
    private final HashMap<Integer, Integer> timeCriteriaTrees = new HashMap<>();
    protected HashMap<Integer, CriteriaProgress> criteriaProgress = new HashMap<>();

    public void reset() {
        for (var iter : criteriaProgress.entrySet()) {
            sendCriteriaProgressRemoved(iter.getKey());
        }

        criteriaProgress.clear();
    }

    /**
     * this function will be called whenever the user might have done a criteria relevant action
     *
     * @param type
     * @param miscValue1
     * @param miscValue2
     * @param miscValue3
     * @param refe
     * @param referencePlayer
     */

    public final void updateCriteria(CriteriaType type, long miscValue1, long miscValue2, long miscValue3, WorldObject refe) {
        updateCriteria(type, miscValue1, miscValue2, miscValue3, refe, null);
    }

    public final void updateCriteria(CriteriaType type, long miscValue1, long miscValue2, long miscValue3) {
        updateCriteria(type, miscValue1, miscValue2, miscValue3, null, null);
    }

    public final void updateCriteria(CriteriaType type, long miscValue1, long miscValue2) {
        updateCriteria(type, miscValue1, miscValue2, 0, null, null);
    }

    public final void updateCriteria(CriteriaType type, long miscValue1) {
        updateCriteria(type, miscValue1, 0, 0, null, null);
    }

    public final void updateCriteria(CriteriaType type) {
        updateCriteria(type, 0, 0, 0, null, null);
    }

    public final void updateCriteria(CriteriaType type, long miscValue1, long miscValue2, long miscValue3, WorldObject refe, Player referencePlayer) {
        if (type.getValue() >= CriteriaType.count.getValue()) {
            Log.outDebug(LogFilter.achievement, "UpdateCriteria: Wrong criteria type {0}", type);

            return;
        }

        if (!referencePlayer) {
            Log.outDebug(LogFilter.achievement, "UpdateCriteria: Player is NULL! Cant update criteria");

            return;
        }

        // Disable for GameMasters with GM-mode enabled or for players that don't have the related RBAC permission
        if (referencePlayer.isGameMaster() || referencePlayer.getSession().hasPermission(RBACPermissions.CannotEarnAchievements)) {
            Log.outDebug(LogFilter.achievement, String.format("CriteriaHandler::UpdateCriteria: [Player %1$s %2$s]", referencePlayer.getName(), (referencePlayer.isGameMaster() ? "GM mode on" : "disallowed by RBAC")) + String.format(" %1$s, %2$s (%3$s), %4$s, %5$s, %6$s", getOwnerInfo(), type, (int) type.getValue(), miscValue1, miscValue2, miscValue3));

            return;
        }

        Log.outDebug(LogFilter.achievement, "UpdateCriteria({0}, {1}, {2}, {3}) {4}", type, type, miscValue1, miscValue2, miscValue3, getOwnerInfo());

        var criteriaList = getCriteriaByType(type, (int) miscValue1);

        for (var criteria : criteriaList) {
            var trees = global.getCriteriaMgr().getCriteriaTreesByCriteria(criteria.id);

            if (!canUpdateCriteria(criteria, trees, miscValue1, miscValue2, miscValue3, refe, referencePlayer)) {
                continue;
            }

            // requirements not found in the dbc
            var data = global.getCriteriaMgr().getCriteriaDataSet(criteria);

            if (data != null) {
                if (!data.meets(referencePlayer, refe, (int) miscValue1, (int) miscValue2)) {
                    continue;
                }
            }

            switch (type) {
                // std. case: increment at 1
                case WinBattleground:
                case TotalRespecs:
                case LoseDuel:
                case ItemsPostedAtAuction:
                case AuctionsWon: // FIXME: for online player only currently
                case RollAnyNeed:
                case RollAnyGreed:
                case AbandonAnyQuest:
                case BuyTaxi:
                case AcceptSummon:
                case LootAnyItem:
                case ObtainAnyItem:
                case DieAnywhere:
                case CompleteDailyQuest:
                case ParticipateInBattleground:
                case DieOnMap:
                case DieInInstance:
                case KilledByCreature:
                case KilledByPlayer:
                case DieFromEnviromentalDamage:
                case BeSpellTarget:
                case GainAura:
                case CastSpell:
                case LandTargetedSpellOnTarget:
                case WinAnyRankedArena:
                case UseItem:
                case RollNeed:
                case RollGreed:
                case DoEmote:
                case UseGameobject:
                case CatchFishInFishingHole:
                case WinDuel:
                case DeliverKillingBlowToClass:
                case DeliverKillingBlowToRace:
                case TrackedWorldStateUIModified:
                case EarnHonorableKill:
                case KillPlayer:
                case DeliveredKillingBlow:
                case PVPKillInArea:
                case WinArena: // This also behaves like CriteriaType.WinAnyRankedArena
                case PlayerTriggerGameEvent:
                case Login:
                case AnyoneTriggerGameEventScenario:
                case BattlePetReachLevel:
                case ActivelyEarnPetLevel:
                case PlaceGarrisonBuilding:
                case ActivateAnyGarrisonBuilding:
                case HonorLevelIncrease:
                case PrestigeLevelIncrease:
                case LearnAnyTransmogInSlot:
                case CollectTransmogSetFromGroup:
                case CompleteAnyReplayQuest:
                case BuyItemsFromVendors:
                case SellItemsToVendors:
                case EnterTopLevelArea:
                    setCriteriaProgress(criteria, 1, referencePlayer, ProgressType.Accumulate);

                    break;
                // std case: increment at miscValue1
                case MoneyEarnedFromSales:
                case MoneySpentOnRespecs:
                case MoneyEarnedFromQuesting:
                case MoneySpentOnTaxis:
                case MoneySpentAtBarberShop:
                case MoneySpentOnPostage:
                case MoneyLootedFromCreatures:
                case MoneyEarnedFromAuctions: // FIXME: for online player only currently
                case TotalDamageTaken:
                case TotalHealReceived:
                case CompletedLFGDungeonWithStrangers:
                case DamageDealt:
                case HealingDone:
                case EarnArtifactXPForAzeriteItem:
                    setCriteriaProgress(criteria, miscValue1, referencePlayer, ProgressType.Accumulate);

                    break;
                case KillCreature:
                case KillAnyCreature:
                case GetLootByType:
                case AcquireItem:
                case LootItem:
                case CurrencyGained:
                    setCriteriaProgress(criteria, miscValue2, referencePlayer, ProgressType.Accumulate);

                    break;
                // std case: high value at miscValue1
                case HighestAuctionBid:
                case HighestAuctionSale: // FIXME: for online player only currently
                case HighestDamageDone:
                case HighestDamageTaken:
                case HighestHealCast:
                case HighestHealReceived:
                case AzeriteLevelReached:
                    setCriteriaProgress(criteria, miscValue1, referencePlayer, ProgressType.Highest);

                    break;
                case ReachLevel:
                    setCriteriaProgress(criteria, referencePlayer.getLevel(), referencePlayer);

                    break;
                case SkillRaised:
                    int skillvalue = referencePlayer.getBaseSkillValue(SkillType.forValue(criteria.entry.asset));

                    if (skillvalue != 0) {
                        setCriteriaProgress(criteria, skillvalue, referencePlayer);
                    }

                    break;
                case AchieveSkillStep:
                    int maxSkillvalue = referencePlayer.getPureMaxSkillValue(SkillType.forValue(criteria.entry.asset));

                    if (maxSkillvalue != 0) {
                        setCriteriaProgress(criteria, maxSkillvalue, referencePlayer);
                    }

                    break;
                case CompleteQuestsCount:
                    setCriteriaProgress(criteria, (int) referencePlayer.getRewardedQuestCount(), referencePlayer);

                    break;
                case CompleteAnyDailyQuestPerDay: {
                    var nextDailyResetTime = global.getWorldMgr().getNextDailyQuestsResetTime();
                    var progress = getCriteriaProgress(criteria);

                    if (miscValue1 == 0) // Login case.
                    {
                        // reset if player missed one day.
                        if (progress != null && progress.date < (nextDailyResetTime - 2 * time.Day)) {
                            setCriteriaProgress(criteria, 0, referencePlayer);
                        }

                        continue;
                    }

                    ProgressType progressType;

                    if (progress == null) {
                        // 1st time. Start count.
                        progressType = ProgressType.set;
                    } else if (progress.date < (nextDailyResetTime - 2 * time.Day)) {
                        // last progress is older than 2 days. Player missed 1 day => Restart count.
                        progressType = ProgressType.set;
                    } else if (progress.date < (nextDailyResetTime - time.Day)) {
                        // last progress is between 1 and 2 days. => 1st time of the day.
                        progressType = ProgressType.Accumulate;
                    } else {
                        // last progress is within the day before the reset => Already counted today.
                        continue;
                    }

                    setCriteriaProgress(criteria, 1, referencePlayer, progressType);

                    break;
                }
                case CompleteQuestsInZone: {
                    if (miscValue1 != 0) {
                        setCriteriaProgress(criteria, 1, referencePlayer, ProgressType.Accumulate);
                    } else // login case
                    {
                        int counter = 0;

                        var rewQuests = referencePlayer.getRewardedQuests();

                        for (var id : rewQuests) {
                            var quest = global.getObjectMgr().getQuestTemplate(id);

                            if (quest != null && quest.questSortID >= 0 && quest.questSortID == criteria.entry.asset) {
                                ++counter;
                            }
                        }

                        setCriteriaProgress(criteria, counter, referencePlayer);
                    }

                    break;
                }
                case MaxDistFallenWithoutDying:
                    // miscValue1 is the ingame fallheight*100 as stored in dbc
                    setCriteriaProgress(criteria, miscValue1, referencePlayer);

                    break;
                case CompleteQuest:
                case LearnOrKnowSpell:
                case RevealWorldMapOverlay:
                case GotHaircut:
                case EquipItemInSlot:
                case EquipItem:
                case EarnAchievement:
                case RecruitGarrisonFollower:
                case LearnedNewPet:
                case ActivelyReachLevel:
                    setCriteriaProgress(criteria, 1, referencePlayer);

                    break;
                case BankSlotsPurchased:
                    setCriteriaProgress(criteria, referencePlayer.getBankBagSlotCount(), referencePlayer);

                    break;
                case ReputationGained: {
                    var reputation = referencePlayer.getReputationMgr().getReputation(criteria.entry.asset);

                    if (reputation > 0) {
                        setCriteriaProgress(criteria, (int) reputation, referencePlayer);
                    }

                    break;
                }
                case TotalExaltedFactions:
                    setCriteriaProgress(criteria, referencePlayer.getReputationMgr().getExaltedFactionCount(), referencePlayer);

                    break;
                case LearnSpellFromSkillLine:
                case LearnTradeskillSkillLine: {
                    int spellCount = 0;


                    for (var(spellId, _) : referencePlayer.getSpellMap()) {
                        var bounds = global.getSpellMgr().getSkillLineAbilityMapBounds(spellId);

                        for (var skill : bounds) {
                            if (skill.skillLine == criteria.entry.asset) {
                                // do not add couter twice if by any chance skill is listed twice in dbc (eg. skill 777 and spell 22717)
                                ++spellCount;

                                break;
                            }
                        }
                    }

                    setCriteriaProgress(criteria, spellCount, referencePlayer);

                    break;
                }
                case TotalReveredFactions:
                    setCriteriaProgress(criteria, referencePlayer.getReputationMgr().getReveredFactionCount(), referencePlayer);

                    break;
                case TotalHonoredFactions:
                    setCriteriaProgress(criteria, referencePlayer.getReputationMgr().getHonoredFactionCount(), referencePlayer);

                    break;
                case TotalFactionsEncountered:
                    setCriteriaProgress(criteria, referencePlayer.getReputationMgr().getVisibleFactionCount(), referencePlayer);

                    break;
                case HonorableKills:
                    setCriteriaProgress(criteria, referencePlayer.getActivePlayerData().lifetimeHonorableKills, referencePlayer);

                    break;
                case MostMoneyOwned:
                    setCriteriaProgress(criteria, referencePlayer.getMoney(), referencePlayer, ProgressType.Highest);

                    break;
                case EarnAchievementPoints:
                    if (miscValue1 == 0) {
                        continue;
                    }

                    setCriteriaProgress(criteria, miscValue1, referencePlayer, ProgressType.Accumulate);

                    break;
                case EarnPersonalArenaRating: {
                    var reqTeamType = criteria.entry.asset;

                    if (miscValue1 != 0) {
                        if (miscValue2 != reqTeamType) {
                            continue;
                        }

                        setCriteriaProgress(criteria, miscValue1, referencePlayer, ProgressType.Highest);
                    } else // login case
                    {
                        for (byte arena_slot = 0; arena_slot < SharedConst.MaxArenaSlot; ++arena_slot) {
                            var teamId = referencePlayer.getArenaTeamId(arena_slot);

                            if (teamId == 0) {
                                continue;
                            }

                            var team = global.getArenaTeamMgr().getArenaTeamById(teamId);

                            if (team == null || team.getArenaType() != reqTeamType) {
                                continue;
                            }

                            var member = team.getMember(referencePlayer.getGUID());

                            if (member != null) {
                                setCriteriaProgress(criteria, member.personalRating, referencePlayer, ProgressType.Highest);

                                break;
                            }
                        }
                    }

                    break;
                }
                case UniquePetsOwned:
                    setCriteriaProgress(criteria, referencePlayer.getSession().getBattlePetMgr().getPetUniqueSpeciesCount(), referencePlayer);

                    break;
                case GuildAttainedLevel:
                    setCriteriaProgress(criteria, miscValue1, referencePlayer);

                    break;
                // FIXME: not triggered in code as result, need to implement
                case RunInstance:
                case ParticipateInArena:
                case EarnTeamArenaRating:
                case EarnTitle:
                case MoneySpentOnGuildRepair:
                case CreatedItemsByCastingSpell:
                case FishInAnyPool:
                case GuildBankTabsPurchased:
                case EarnGuildAchievementPoints:
                case WinAnyBattleground:
                case EarnBattlegroundRating:
                case GuildTabardCreated:
                case CompleteQuestsCountForGuild:
                case HonorableKillsForGuild:
                case KillAnyCreatureForGuild:
                case CompleteAnyResearchProject:
                case CompleteGuildChallenge:
                case CompleteAnyGuildChallenge:
                case CompletedLFRDungeon:
                case AbandonedLFRDungeon:
                case KickInitiatorInLFRDungeon:
                case KickVoterInLFRDungeon:
                case KickTargetInLFRDungeon:
                case GroupedTankLeftEarlyInLFRDungeon:
                case CompleteAnyScenario:
                case CompleteScenario:
                case AccountObtainPetThroughBattle:
                case WinPetBattle:
                case PlayerObtainPetThroughBattle:
                case EnterArea:
                case LeaveArea:
                case DefeatDungeonEncounter:
                case ActivateGarrisonBuilding:
                case UpgradeGarrison:
                case StartAnyGarrisonMissionWithFollowerType:
                case SucceedAnyGarrisonMissionWithFollowerType:
                case SucceedGarrisonMission:
                case RecruitAnyGarrisonFollower:
                case LearnAnyGarrisonBlueprint:
                case CollectGarrisonShipment:
                case ItemLevelChangedForGarrisonFollower:
                case LevelChangedForGarrisonFollower:
                case LearnToy:
                case LearnAnyToy:
                case LearnAnyHeirloom:
                case FindResearchObject:
                case ExhaustAnyResearchSite:
                case CompleteInternalCriteria:
                case CompleteAnyChallengeMode:
                case KilledAllUnitsInSpawnRegion:
                case CompleteChallengeMode:
                case CreatedItemsByCastingSpellWithLimit:
                case BattlePetAchievementPointsEarned:
                case ReleasedSpirit:
                case AccountKnownPet:
                case DefeatDungeonEncounterWhileElegibleForLoot:
                case CompletedLFGDungeon:
                case KickInitiatorInLFGDungeon:
                case KickVoterInLFGDungeon:
                case KickTargetInLFGDungeon:
                case AbandonedLFGDungeon:
                case GroupedTankLeftEarlyInLFGDungeon:
                case EnterAreaTriggerWithActionSet:
                case StartGarrisonMission:
                case QualityUpgradedForGarrisonFollower:
                case EarnArtifactXP:
                case AnyArtifactPowerRankPurchased:
                case CompleteResearchGarrisonTalent:
                case RecruitAnyGarrisonTroop:
                case CompleteAnyWorldQuest:
                case ParagonLevelIncreaseWithFaction:
                case PlayerHasEarnedHonor:
                case ChooseRelicTalent:
                case AccountHonorLevelReached:
                case MythicPlusCompleted:
                case SocketAnySoulbindConduit:
                case ObtainAnyItemWithCurrencyValue:
                case EarnExpansionLevel:
                case LearnTransmog:
                    break; // Not implemented yet :(
            }

            for (var tree : trees) {
                if (isCompletedCriteriaTree(tree)) {
                    completedCriteriaTree(tree, referencePlayer);
                }

                afterCriteriaTreeUpdate(tree, referencePlayer);
            }
        }
    }

    public final void updateTimedCriteria(int timeDiff) {
        if (!timeCriteriaTrees.isEmpty()) {
            for (var key : timeCriteriaTrees.keySet().ToList()) {
                var value = timeCriteriaTrees.get(key);

                // Time is up, remove timer and reset progress
                if (value <= timeDiff) {
                    var criteriaTree = global.getCriteriaMgr().getCriteriaTree(key);

                    if (criteriaTree.criteria != null) {
                        removeCriteriaProgress(criteriaTree.criteria);
                    }

                    timeCriteriaTrees.remove(key);
                } else {
                    timeCriteriaTrees.put(key, timeCriteriaTrees.get(key) - timeDiff);
                }
            }
        }
    }


    public final void startCriteriaTimer(CriteriaStartEvent startEvent, int entry) {
        startCriteriaTimer(startEvent, entry, 0);
    }

    public final void startCriteriaTimer(CriteriaStartEvent startEvent, int entry, int timeLost) {
        var criteriaList = global.getCriteriaMgr().getTimedCriteriaByType(startEvent);

        for (var criteria : criteriaList) {
            if (criteria.entry.StartAsset != entry) {
                continue;
            }

            var trees = global.getCriteriaMgr().getCriteriaTreesByCriteria(criteria.id);
            var canStart = false;

            for (var tree : trees) {
                if ((!timeCriteriaTrees.containsKey(tree.id) || criteria.entry.getFlags().hasFlag(CriteriaFlags.ResetOnStart)) && !isCompletedCriteriaTree(tree)) {
                    // Start the timer
                    if (criteria.entry.StartTimer * time.InMilliseconds > timeLost) {
                        timeCriteriaTrees.put(tree.id, (int) (criteria.entry.StartTimer * time.InMilliseconds - timeLost));
                        canStart = true;
                    }
                }
            }

            if (!canStart) {
                continue;
            }

            // and at client too
            setCriteriaProgress(criteria, 0, null, ProgressType.set);
        }
    }

    public final void removeCriteriaTimer(CriteriaStartEvent startEvent, int entry) {
        var criteriaList = global.getCriteriaMgr().getTimedCriteriaByType(startEvent);

        for (var criteria : criteriaList) {
            if (criteria.entry.StartAsset != entry) {
                continue;
            }

            var trees = global.getCriteriaMgr().getCriteriaTreesByCriteria(criteria.id);

            // Remove the timer from all trees
            for (var tree : trees) {
                timeCriteriaTrees.remove(tree.id);
            }

            // remove progress
            removeCriteriaProgress(criteria);
        }
    }

    public final CriteriaProgress getCriteriaProgress(Criteria entry) {
        return criteriaProgress.get(entry.id);
    }


    public final void setCriteriaProgress(Criteria criteria, long changeValue, Player referencePlayer) {
        setCriteriaProgress(criteria, changeValue, referencePlayer, ProgressType.set);
    }

    public final void setCriteriaProgress(Criteria criteria, long changeValue, Player referencePlayer, ProgressType progressType) {
        // Don't allow to cheat - doing timed criteria without timer active
        ArrayList<CriteriaTree> trees = null;

        if (criteria.entry.startTimer != 0) {
            trees = global.getCriteriaMgr().getCriteriaTreesByCriteria(criteria.id);

            if (trees.isEmpty()) {
                return;
            }

            var hasTreeForTimed = false;

            for (var tree : trees) {
                var timedIter = timeCriteriaTrees.get(tree.id);

                if (timedIter != 0) {
                    hasTreeForTimed = true;

                    break;
                }
            }

            if (!hasTreeForTimed) {
                return;
            }
        }

        Log.outDebug(LogFilter.achievement, "SetCriteriaProgress({0}, {1}) for {2}", criteria.id, changeValue, getOwnerInfo());

        var progress = getCriteriaProgress(criteria);

        if (progress == null) {
            // not create record for 0 counter but allow it for timed criteria
            // we will need to send 0 progress to client to start the timer
            if (changeValue == 0 && criteria.entry.startTimer == 0) {
                return;
            }

            progress = new criteriaProgress();
            progress.counter = changeValue;
        } else {
            long newValue = 0;

            switch (progressType) {
                case Set:
                    newValue = changeValue;

                    break;
                case Accumulate: {
                    // avoid overflow
                    var max_value = Long.MAX_VALUE;
                    newValue = max_value - progress.counter > changeValue ? progress.counter + changeValue : max_value;

                    break;
                }
                case Highest:
                    newValue = progress.counter < changeValue ? changeValue : progress.counter;

                    break;
            }

            // not update (not mark as changed) if counter will have same value
            if (progress.counter == newValue && criteria.entry.startTimer == 0) {
                return;
            }

            progress.counter = newValue;
        }

        progress.changed = true;
        progress.date = GameTime.getGameTime(); // set the date to the latest update.
        progress.playerGUID = referencePlayer ? referencePlayer.getGUID() : ObjectGuid.Empty;
        criteriaProgress.put(criteria.id, progress);

        var timeElapsed = duration.Zero;

        if (criteria.entry.startTimer != 0) {
            for (var tree : trees) {
                var timed = timeCriteriaTrees.get(tree.id);

                if (timed != 0) {
                    // Client expects this in packet
                    timeElapsed = duration.FromSeconds(criteria.entry.StartTimer - (timed / time.InMilliseconds));

                    // Remove the timer, we wont need it anymore
                    if (isCompletedCriteriaTree(tree)) {
                        timeCriteriaTrees.remove(tree.id);
                    }
                }
            }
        }

        sendCriteriaUpdate(criteria, progress, timeElapsed, true);
    }

    public final void removeCriteriaProgress(Criteria criteria) {
        if (criteria == null) {
            return;
        }

        if (!criteriaProgress.containsKey(criteria.id)) {
            return;
        }

        sendCriteriaProgressRemoved(criteria.id);

        criteriaProgress.remove(criteria.id);
    }

    public final boolean isCompletedCriteriaTree(CriteriaTree tree) {
        if (!canCompleteCriteriaTree(tree)) {
            return false;
        }

        long requiredCount = tree.entry.amount;

        switch (CriteriaTreeOperator.forValue(tree.entry.Operator)) {
            case Complete:
                return tree.criteria != null && isCompletedCriteria(tree.criteria, requiredCount);
            case NotComplete:
                return tree.criteria == null || !isCompletedCriteria(tree.criteria, requiredCount);
            case CompleteAll:
                for (var node : tree.children) {
                    if (!isCompletedCriteriaTree(node)) {
                        return false;
                    }
                }

                return true;
            case Sum: {
                long progress = 0;

                CriteriaManager.walkCriteriaTree(tree, criteriaTree ->
                {
                    if (criteriaTree.criteria != null) {
                        var criteriaProgress = getCriteriaProgress(criteriaTree.criteria);

                        if (criteriaProgress != null) {
                            progress += criteriaProgress.counter;
                        }
                    }
                });

                return progress >= requiredCount;
            }
            case Highest: {
                long progress = 0;

                CriteriaManager.walkCriteriaTree(tree, criteriaTree ->
                {
                    if (criteriaTree.criteria != null) {
                        var criteriaProgress = getCriteriaProgress(criteriaTree.criteria);

                        if (criteriaProgress != null) {
                            if (criteriaProgress.counter > progress) {
                                progress = criteriaProgress.counter;
                            }
                        }
                    }
                });

                return progress >= requiredCount;
            }
            case StartedAtLeast: {
                long progress = 0;

                for (var node : tree.children) {
                    if (node.criteria != null) {
                        var criteriaProgress = getCriteriaProgress(node.criteria);

                        if (criteriaProgress != null) {
                            if (criteriaProgress.counter >= 1) {
                                if (++progress >= requiredCount) {
                                    return true;
                                }
                            }
                        }
                    }
                }

                return false;
            }
            case CompleteAtLeast: {
                long progress = 0;

                for (var node : tree.children) {
                    if (isCompletedCriteriaTree(node)) {
                        if (++progress >= requiredCount) {
                            return true;
                        }
                    }
                }

                return false;
            }
            case ProgressBar: {
                long progress = 0;

                CriteriaManager.walkCriteriaTree(tree, criteriaTree ->
                {
                    if (criteriaTree.criteria != null) {
                        var criteriaProgress = getCriteriaProgress(criteriaTree.criteria);

                        if (criteriaProgress != null) {
                            progress += criteriaProgress.Counter * criteriaTree.entry.amount;
                        }
                    }
                });

                return progress >= requiredCount;
            }
            default:
                break;
        }

        return false;
    }

    public boolean canUpdateCriteriaTree(Criteria criteria, CriteriaTree tree, Player referencePlayer) {
        if ((tree.entry.flags.hasFlag(CriteriaTreeFlags.HordeOnly) && referencePlayer.getTeam() != Team.Horde) || (tree.entry.flags.hasFlag(CriteriaTreeFlags.AllianceOnly) && referencePlayer.getTeam() != Team.ALLIANCE)) {
            Log.outTrace(LogFilter.achievement, "CriteriaHandler.CanUpdateCriteriaTree: (Id: {0} Type {1} CriteriaTree {2}) Wrong faction", criteria.id, criteria.entry.type, tree.entry.id);

            return false;
        }

        return true;
    }

    public boolean canCompleteCriteriaTree(CriteriaTree tree) {
        return true;
    }

    public final boolean modifierTreeSatisfied(ModifierTreeNode tree, long miscValue1, long miscValue2, WorldObject refe, Player referencePlayer) {
        switch (ModifierTreeOperator.forValue(tree.entry.Operator)) {
            case SingleTrue:
                return tree.entry.type != 0 && modifierSatisfied(tree.entry, miscValue1, miscValue2, refe, referencePlayer);
            case SingleFalse:
                return tree.entry.type != 0 && !modifierSatisfied(tree.entry, miscValue1, miscValue2, refe, referencePlayer);
            case All:
                for (var node : tree.children) {
                    if (!modifierTreeSatisfied(node, miscValue1, miscValue2, refe, referencePlayer)) {
                        return false;
                    }
                }

                return true;
            case Some: {
                var requiredAmount = Math.max(tree.entry.amount, (byte) 1);

                for (var node : tree.children) {
                    if (modifierTreeSatisfied(node, miscValue1, miscValue2, refe, referencePlayer)) {
                        if (--requiredAmount == 0) {
                            return true;
                        }
                    }
                }

                return false;
            }
            default:
                break;
        }

        return false;
    }

    public void sendAllData(Player receiver) {
    }

    public void sendCriteriaUpdate(Criteria criteria, CriteriaProgress progress, Duration timeElapsed, boolean timedCompleted) {
    }

    public void sendCriteriaProgressRemoved(int criteriaId) {
    }

    public void completedCriteriaTree(CriteriaTree tree, Player referencePlayer) {
    }

    public void afterCriteriaTreeUpdate(CriteriaTree tree, Player referencePlayer) {
    }

    public void sendPacket(ServerPacket data) {
    }

    public boolean requiredAchievementSatisfied(int achievementId) {
        return false;
    }

    public String getOwnerInfo() {
        return "";
    }

    public ArrayList<criteria> getCriteriaByType(CriteriaType type, int asset) {
        return null;
    }

    private boolean isCompletedCriteria(Criteria criteria, long requiredAmount) {
        var progress = getCriteriaProgress(criteria);

        if (progress == null) {
            return false;
        }

        switch (criteria.entry.type) {
            case WinBattleground:
            case KillCreature:
            case ReachLevel:
            case GuildAttainedLevel:
            case SkillRaised:
            case CompleteQuestsCount:
            case CompleteAnyDailyQuestPerDay:
            case CompleteQuestsInZone:
            case DamageDealt:
            case HealingDone:
            case CompleteDailyQuest:
            case MaxDistFallenWithoutDying:
            case BeSpellTarget:
            case GainAura:
            case CastSpell:
            case LandTargetedSpellOnTarget:
            case TrackedWorldStateUIModified:
            case PVPKillInArea:
            case EarnHonorableKill:
            case HonorableKills:
            case AcquireItem:
            case WinAnyRankedArena:
            case EarnPersonalArenaRating:
            case UseItem:
            case LootItem:
            case BankSlotsPurchased:
            case ReputationGained:
            case TotalExaltedFactions:
            case GotHaircut:
            case EquipItemInSlot:
            case RollNeed:
            case RollGreed:
            case DeliverKillingBlowToClass:
            case DeliverKillingBlowToRace:
            case DoEmote:
            case EquipItem:
            case MoneyEarnedFromQuesting:
            case MoneyLootedFromCreatures:
            case UseGameobject:
            case KillPlayer:
            case CatchFishInFishingHole:
            case LearnSpellFromSkillLine:
            case WinDuel:
            case GetLootByType:
            case LearnTradeskillSkillLine:
            case CompletedLFGDungeonWithStrangers:
            case DeliveredKillingBlow:
            case CurrencyGained:
            case PlaceGarrisonBuilding:
            case UniquePetsOwned:
            case BattlePetReachLevel:
            case ActivelyEarnPetLevel:
            case LearnAnyTransmogInSlot:
            case ParagonLevelIncreaseWithFaction:
            case PlayerHasEarnedHonor:
            case ChooseRelicTalent:
            case AccountHonorLevelReached:
            case EarnArtifactXPForAzeriteItem:
            case AzeriteLevelReached:
            case CompleteAnyReplayQuest:
            case BuyItemsFromVendors:
            case SellItemsToVendors:
            case EnterTopLevelArea:
                return progress.counter >= requiredAmount;
            case EarnAchievement:
            case CompleteQuest:
            case LearnOrKnowSpell:
            case RevealWorldMapOverlay:
            case RecruitGarrisonFollower:
            case LearnedNewPet:
            case HonorLevelIncrease:
            case PrestigeLevelIncrease:
            case ActivelyReachLevel:
            case CollectTransmogSetFromGroup:
                return progress.counter >= 1;
            case AchieveSkillStep:
                return progress.counter >= (requiredAmount * 75);
            case EarnAchievementPoints:
                return progress.counter >= 9000;
            case WinArena:
                return requiredAmount != 0 && progress.counter >= requiredAmount;
            case Login:
                return true;
            // handle all statistic-only criteria here
            default:
                break;
        }

        return false;
    }

    private boolean canUpdateCriteria(Criteria criteria, ArrayList<CriteriaTree> trees, long miscValue1, long miscValue2, long miscValue3, WorldObject refe, Player referencePlayer) {
        if (global.getDisableMgr().isDisabledFor(DisableType.criteria, criteria.id, null)) {
            Log.outError(LogFilter.achievement, "CanUpdateCriteria: (Id: {0} Type {1}) Disabled", criteria.id, criteria.entry.type);

            return false;
        }

        var treeRequirementPassed = false;

        for (var tree : trees) {
            if (!canUpdateCriteriaTree(criteria, tree, referencePlayer)) {
                continue;
            }

            treeRequirementPassed = true;

            break;
        }

        if (!treeRequirementPassed) {
            return false;
        }

        if (!requirementsSatisfied(criteria, miscValue1, miscValue2, miscValue3, refe, referencePlayer)) {
            Log.outTrace(LogFilter.achievement, "CanUpdateCriteria: (Id: {0} Type {1}) Requirements not satisfied", criteria.id, criteria.entry.type);

            return false;
        }

        if (criteria.modifier != null && !modifierTreeSatisfied(criteria.modifier, miscValue1, miscValue2, refe, referencePlayer)) {
            Log.outTrace(LogFilter.achievement, "CanUpdateCriteria: (Id: {0} Type {1}) Requirements have not been satisfied", criteria.id, criteria.entry.type);

            return false;
        }

        if (!conditionsSatisfied(criteria, referencePlayer)) {
            Log.outTrace(LogFilter.achievement, "CanUpdateCriteria: (Id: {0} Type {1}) Conditions have not been satisfied", criteria.id, criteria.entry.type);

            return false;
        }

        if (criteria.entry.EligibilityWorldStateID != 0) {
            if (global.getWorldStateMgr().getValue(criteria.entry.EligibilityWorldStateID, referencePlayer.getMap()) != criteria.entry.EligibilityWorldStateValue) {
                return false;
            }
        }

        return true;
    }

    private boolean conditionsSatisfied(Criteria criteria, Player referencePlayer) {
        if (criteria.entry.FailEvent == 0) {
            return true;
        }

        switch (CriteriaFailEvent.forValue(criteria.entry.FailEvent)) {
            case LeaveBattleground:
                if (!referencePlayer.getInBattleground()) {
                    return false;
                }

                break;
            case ModifyPartyStatus:
                if (referencePlayer.getGroup()) {
                    return false;
                }

                break;
            default:
                break;
        }

        return true;
    }

    private boolean requirementsSatisfied(Criteria criteria, long miscValue1, long miscValue2, long miscValue3, WorldObject refe, Player referencePlayer) {
        switch (criteria.entry.type) {
            case AcceptSummon:
            case CompleteDailyQuest:
            case ItemsPostedAtAuction:
            case MaxDistFallenWithoutDying:
            case BuyTaxi:
            case DeliveredKillingBlow:
            case MoneyEarnedFromAuctions:
            case MoneySpentAtBarberShop:
            case MoneySpentOnPostage:
            case MoneySpentOnRespecs:
            case MoneySpentOnTaxis:
            case HighestAuctionBid:
            case HighestAuctionSale:
            case HighestHealReceived:
            case HighestHealCast:
            case HighestDamageDone:
            case HighestDamageTaken:
            case EarnHonorableKill:
            case LootAnyItem:
            case MoneyLootedFromCreatures:
            case LoseDuel:
            case MoneyEarnedFromQuesting:
            case MoneyEarnedFromSales:
            case TotalRespecs:
            case ObtainAnyItem:
            case AbandonAnyQuest:
            case GuildAttainedLevel:
            case RollAnyGreed:
            case RollAnyNeed:
            case KillPlayer:
            case TotalDamageTaken:
            case TotalHealReceived:
            case CompletedLFGDungeonWithStrangers:
            case GotHaircut:
            case WinDuel:
            case WinAnyRankedArena:
            case AuctionsWon:
            case CompleteAnyReplayQuest:
            case BuyItemsFromVendors:
            case SellItemsToVendors:
                if (miscValue1 == 0) {
                    return false;
                }

                break;
            case BankSlotsPurchased:
            case CompleteAnyDailyQuestPerDay:
            case CompleteQuestsCount:
            case EarnAchievementPoints:
            case TotalExaltedFactions:
            case TotalHonoredFactions:
            case TotalReveredFactions:
            case MostMoneyOwned:
            case EarnPersonalArenaRating:
            case TotalFactionsEncountered:
            case ReachLevel:
            case Login:
            case UniquePetsOwned:
                break;
            case EarnAchievement:
                if (!requiredAchievementSatisfied(criteria.entry.asset)) {
                    return false;
                }

                break;
            case WinBattleground:
            case ParticipateInBattleground:
            case DieOnMap:
                if (miscValue1 == 0 || criteria.entry.asset != referencePlayer.getLocation().getMapId()) {
                    return false;
                }

                break;
            case KillCreature:
            case KilledByCreature:
                if (miscValue1 == 0 || criteria.entry.asset != miscValue1) {
                    return false;
                }

                break;
            case SkillRaised:
            case AchieveSkillStep:
                // update at loading or specific skill update
                if (miscValue1 != 0 && miscValue1 != criteria.entry.asset) {
                    return false;
                }

                break;
            case CompleteQuestsInZone:
                if (miscValue1 != 0) {
                    var quest = global.getObjectMgr().getQuestTemplate((int) miscValue1);

                    if (quest == null || quest.questSortID != criteria.entry.asset) {
                        return false;
                    }
                }

                break;
            case DieAnywhere: {
                if (miscValue1 == 0) {
                    return false;
                }

                break;
            }
            case DieInInstance: {
                if (miscValue1 == 0) {
                    return false;
                }

                var map = referencePlayer.isInWorld() ? referencePlayer.getMap() : global.getMapMgr().findMap(referencePlayer.getLocation().getMapId(), referencePlayer.getInstanceId());

                if (!map || !map.isDungeon()) {
                    return false;
                }

                //FIXME: work only for instances where max == min for players
                if (map.toInstanceMap().getMaxPlayers() != criteria.entry.asset) {
                    return false;
                }

                break;
            }
            case KilledByPlayer:
                if (miscValue1 == 0 || !refe || !refe.isTypeId(TypeId.PLAYER)) {
                    return false;
                }

                break;
            case DieFromEnviromentalDamage:
                if (miscValue1 == 0 || miscValue2 != criteria.entry.asset) {
                    return false;
                }

                break;
            case CompleteQuest: {
                // if miscValues != 0, it contains the questID.
                if (miscValue1 != 0) {
                    if (miscValue1 != criteria.entry.asset) {
                        return false;
                    }
                } else {
                    // login case.
                    if (!referencePlayer.getQuestRewardStatus(criteria.entry.asset)) {
                        return false;
                    }
                }

                var data = global.getCriteriaMgr().getCriteriaDataSet(criteria);

                if (data != null) {
                    if (!data.meets(referencePlayer, refe)) {
                        return false;
                    }
                }

                break;
            }
            case BeSpellTarget:
            case GainAura:
            case CastSpell:
            case LandTargetedSpellOnTarget:
                if (miscValue1 == 0 || miscValue1 != criteria.entry.asset) {
                    return false;
                }

                break;
            case LearnOrKnowSpell:
                if (miscValue1 != 0 && miscValue1 != criteria.entry.asset) {
                    return false;
                }

                if (!referencePlayer.hasSpell(criteria.entry.asset)) {
                    return false;
                }

                break;
            case GetLootByType:
                // miscValue1 = itemId - miscValue2 = count of item loot
                // miscValue3 = loot_type (note: 0 = LOOT_CORPSE and then it ignored)
                if (miscValue1 == 0 || miscValue2 == 0 || miscValue3 == 0 || miscValue3 != criteria.entry.asset) {
                    return false;
                }

                break;
            case AcquireItem:
                if (miscValue1 != 0 && criteria.entry.asset != miscValue1) {
                    return false;
                }

                break;
            case UseItem:
            case LootItem:
            case EquipItem:
                if (miscValue1 == 0 || criteria.entry.asset != miscValue1) {
                    return false;
                }

                break;
            case RevealWorldMapOverlay: {
                var worldOverlayEntry = CliDB.WorldMapOverlayStorage.get(criteria.entry.asset);

                if (worldOverlayEntry == null) {
                    break;
                }

                var matchFound = false;

                for (var j = 0; j < SharedConst.MaxWorldMapOverlayArea; ++j) {
                    var area = CliDB.AreaTableStorage.get(worldOverlayEntry.AreaID[j]);

                    if (area == null) {
                        break;
                    }

                    if (area.AreaBit < 0) {
                        continue;
                    }

                    var playerIndexOffset = (int) area.AreaBit / activePlayerData.EXPLOREDZONESBITS;

                    if (playerIndexOffset >= PlayerConst.EXPLOREDZONESSIZE) {
                        continue;
                    }

                    var mask = 1 << (int) ((int) area.AreaBit % activePlayerData.EXPLOREDZONESBITS);

                    if ((boolean) (referencePlayer.getActivePlayerData().exploredZones.get(playerIndexOffset) & mask)) {
                        matchFound = true;

                        break;
                    }
                }

                if (!matchFound) {
                    return false;
                }

                break;
            }
            case ReputationGained:
                if (miscValue1 != 0 && miscValue1 != criteria.entry.asset) {
                    return false;
                }

                break;
            case EquipItemInSlot:
            case LearnAnyTransmogInSlot:
                // miscValue1 = EquipmentSlot miscValue2 = itemid | itemModifiedAppearanceId
                if (miscValue2 == 0 || miscValue1 != criteria.entry.asset) {
                    return false;
                }

                break;
            case RollNeed:
            case RollGreed: {
                // miscValue1 = itemid miscValue2 = diced value
                if (miscValue1 == 0 || miscValue2 != criteria.entry.asset) {
                    return false;
                }

                var proto = global.getObjectMgr().getItemTemplate((int) miscValue1);

                if (proto == null) {
                    return false;
                }

                break;
            }
            case DoEmote:
                if (miscValue1 == 0 || miscValue1 != criteria.entry.asset) {
                    return false;
                }

                break;
            case DamageDealt:
            case HealingDone:
                if (miscValue1 == 0) {
                    return false;
                }

                if (criteria.entry.FailEvent == CriteriaFailEvent.LeaveBattleground.getValue()) {
                    if (!referencePlayer.getInBattleground()) {
                        return false;
                    }

                    // map specific case (BG in fact) expected player targeted damage/heal
                    if (!refe || !refe.isTypeId(TypeId.PLAYER)) {
                        return false;
                    }
                }

                break;
            case UseGameobject:
            case CatchFishInFishingHole:
                if (miscValue1 == 0 || miscValue1 != criteria.entry.asset) {
                    return false;
                }

                break;
            case LearnSpellFromSkillLine:
            case LearnTradeskillSkillLine:
                if (miscValue1 != 0 && miscValue1 != criteria.entry.asset) {
                    return false;
                }

                break;
            case DeliverKillingBlowToClass:
                if (miscValue1 == 0 || miscValue1 != criteria.entry.asset) {
                    return false;
                }

                break;
            case DeliverKillingBlowToRace:
                if (miscValue1 == 0 || miscValue1 != criteria.entry.asset) {
                    return false;
                }

                break;
            case TrackedWorldStateUIModified:
                if (miscValue1 == 0 || miscValue1 != criteria.entry.asset) {
                    return false;
                }

                break;
            case PVPKillInArea:
            case EnterTopLevelArea:
                if (miscValue1 == 0 || miscValue1 != criteria.entry.asset) {
                    return false;
                }

                break;
            case CurrencyGained:
                if (miscValue1 == 0 || miscValue2 == 0 || (long) miscValue2 < 0 || miscValue1 != criteria.entry.asset) {
                    return false;
                }

                break;
            case WinArena:
                if (miscValue1 != criteria.entry.asset) {
                    return false;
                }

                break;
            case EarnTeamArenaRating:
                return false;
            case PlaceGarrisonBuilding:
            case ActivateGarrisonBuilding:
                if (miscValue1 != criteria.entry.asset) {
                    return false;
                }

                break;
            case RecruitGarrisonFollower:
                if (miscValue1 != criteria.entry.asset) {
                    return false;
                }

                break;
            case CollectTransmogSetFromGroup:
                if (miscValue1 != criteria.entry.asset) {
                    return false;
                }

                break;
            case BattlePetReachLevel:
            case ActivelyEarnPetLevel:
                if (miscValue1 == 0 || miscValue2 == 0 || miscValue2 != criteria.entry.asset) {
                    return false;
                }

                break;
            case ActivelyReachLevel:
                if (miscValue1 == 0 || miscValue1 != criteria.entry.asset) {
                    return false;
                }

                break;
            default:
                break;
        }

        return true;
    }

    private boolean modifierSatisfied(ModifierTreeRecord modifier, long miscValue1, long miscValue2, WorldObject refe, Player referencePlayer) {
        var reqValue = modifier.asset;
        var secondaryAsset = modifier.SecondaryAsset;
        var tertiaryAsset = modifier.TertiaryAsset;

        switch (ModifierTreeType.forValue(modifier.type)) {
            case PlayerInebriationLevelEqualOrGreaterThan: // 1
            {
                var inebriation = (int) Math.min(Math.max(referencePlayer.getDrunkValue(), referencePlayer.getPlayerData().fakeInebriation), 100);

                if (inebriation < reqValue) {
                    return false;
                }

                break;
            }
            case PlayerMeetsCondition: // 2
            {
                var playerCondition = CliDB.PlayerConditionStorage.get(reqValue);

                if (playerCondition == null || !ConditionManager.isPlayerMeetingCondition(referencePlayer, playerCondition)) {
                    return false;
                }

                break;
            }
            case MinimumItemLevel: // 3
            {
                // miscValue1 is itemid
                var item = global.getObjectMgr().getItemTemplate((int) miscValue1);

                if (item == null || item.getBaseItemLevel() < reqValue) {
                    return false;
                }

                break;
            }
            case TargetCreatureId: // 4
                if (refe == null || refe.getEntry() != reqValue) {
                    return false;
                }

                break;
            case TargetIsPlayer: // 5
                if (refe == null || !refe.isTypeId(TypeId.PLAYER)) {
                    return false;
                }

                break;
            case TargetIsDead: // 6
                if (refe == null || !refe.isUnit() || refe.toUnit().isAlive()) {
                    return false;
                }

                break;
            case TargetIsOppositeFaction: // 7
                if (refe == null || !referencePlayer.isHostileTo(refe)) {
                    return false;
                }

                break;
            case PlayerHasAura: // 8
                if (!referencePlayer.hasAura(reqValue)) {
                    return false;
                }

                break;
            case PlayerHasAuraEffect: // 9
                if (!referencePlayer.hasAuraType(AuraType.forValue(reqValue))) {
                    return false;
                }

                break;
            case TargetHasAura: // 10
                if (refe == null || !refe.isUnit() || !refe.toUnit().hasAura(reqValue)) {
                    return false;
                }

                break;
            case TargetHasAuraEffect: // 11
                if (refe == null || !refe.isUnit() || !refe.toUnit().hasAuraType(AuraType.forValue(reqValue))) {
                    return false;
                }

                break;
            case TargetHasAuraState: // 12
                if (refe == null || !refe.isUnit() || !refe.toUnit().hasAuraState(AuraStateType.forValue(reqValue))) {
                    return false;
                }

                break;
            case PlayerHasAuraState: // 13
                if (!referencePlayer.hasAuraState(AuraStateType.forValue(reqValue))) {
                    return false;
                }

                break;
            case ItemQualityIsAtLeast: // 14
            {
                // miscValue1 is itemid
                var item = global.getObjectMgr().getItemTemplate((int) miscValue1);

                if (item == null || (int) item.getQuality().getValue() < reqValue) {
                    return false;
                }

                break;
            }
            case ItemQualityIsExactly: // 15
            {
                // miscValue1 is itemid
                var item = global.getObjectMgr().getItemTemplate((int) miscValue1);

                if (item == null || (int) item.getQuality().getValue() != reqValue) {
                    return false;
                }

                break;
            }
            case PlayerIsAlive: // 16
                if (referencePlayer.isDead()) {
                    return false;
                }

                break;
            case PlayerIsInArea: // 17
            {
                int zoneId;
                tangible.OutObject<Integer> tempOut_zoneId = new tangible.OutObject<Integer>();
                int areaId;
                tangible.OutObject<Integer> tempOut_areaId = new tangible.OutObject<Integer>();
                referencePlayer.getZoneAndAreaId(tempOut_zoneId, tempOut_areaId);
                areaId = tempOut_areaId.outArgValue;
                zoneId = tempOut_zoneId.outArgValue;

                if (zoneId != reqValue && areaId != reqValue) {
                    return false;
                }

                break;
            }
            case TargetIsInArea: // 18
            {
                if (refe == null) {
                    return false;
                }

                int zoneId;
                tangible.OutObject<Integer> tempOut_zoneId2 = new tangible.OutObject<Integer>();
                int areaId;
                tangible.OutObject<Integer> tempOut_areaId2 = new tangible.OutObject<Integer>();
                refe.getZoneAndAreaId(tempOut_zoneId2, tempOut_areaId2);
                areaId = tempOut_areaId2.outArgValue;
                zoneId = tempOut_zoneId2.outArgValue;

                if (zoneId != reqValue && areaId != reqValue) {
                    return false;
                }

                break;
            }
            case ItemId: // 19
                if (miscValue1 != reqValue) {
                    return false;
                }

                break;
            case LegacyDungeonDifficulty: // 20
            {
                var difficulty = CliDB.DifficultyStorage.get(referencePlayer.getMap().getDifficultyID());

                if (difficulty == null || difficulty.OldEnumValue == -1 || difficulty.OldEnumValue != reqValue) {
                    return false;
                }

                break;
            }
            case PlayerToTargetLevelDeltaGreaterThan: // 21
                if (refe == null || !refe.isUnit() || referencePlayer.getLevel() < refe.toUnit().getLevel() + reqValue) {
                    return false;
                }

                break;
            case TargetToPlayerLevelDeltaGreaterThan: // 22
                if (!refe || !refe.isUnit() || referencePlayer.getLevel() + reqValue < refe.toUnit().getLevel()) {
                    return false;
                }

                break;
            case PlayerLevelEqualTargetLevel: // 23
                if (!refe || !refe.isUnit() || referencePlayer.getLevel() != refe.toUnit().getLevel()) {
                    return false;
                }

                break;
            case PlayerInArenaWithTeamSize: // 24
            {
                var bg = referencePlayer.getBattleground();

                if (!bg || !bg.isArena() || bg.getArenaType() != ArenaTypes.forValue(reqValue)) {
                    return false;
                }

                break;
            }
            case PlayerRace: // 25
                if ((int) referencePlayer.getRace().getValue() != reqValue) {
                    return false;
                }

                break;
            case PlayerClass: // 26
                if ((int) referencePlayer.getUnitClass().getValue() != reqValue) {
                    return false;
                }

                break;
            case TargetRace: // 27
                if (refe == null || !refe.isUnit() || refe.toUnit().getRace() != race.forValue(reqValue)) {
                    return false;
                }

                break;
            case TargetClass: // 28
                if (refe == null || !refe.isUnit() || refe.toUnit().getUnitClass() != playerClass.forValue(reqValue)) {
                    return false;
                }

                break;
            case LessThanTappers: // 29
                if (referencePlayer.getGroup() && referencePlayer.getGroup().getMembersCount() >= reqValue) {
                    return false;
                }

                break;
            case CreatureType: // 30
            {
                if (refe == null) {
                    return false;
                }

                if (!refe.isUnit() || refe.toUnit().getCreatureType() != creatureType.forValue(reqValue)) {
                    return false;
                }

                break;
            }
            case CreatureFamily: // 31
            {
                if (!refe) {
                    return false;
                }

                if (!refe.isCreature() || refe.toCreature().getCreatureTemplate().family != creatureFamily.forValue(reqValue)) {
                    return false;
                }

                break;
            }
            case PlayerMap: // 32
                if (referencePlayer.getLocation().getMapId() != reqValue) {
                    return false;
                }

                break;
            case ClientVersionEqualOrLessThan: // 33
                if (reqValue < global.getRealmMgr().GetMinorMajorBugfixVersionForBuild(global.getWorldMgr().getRealm().Build)) {
                    return false;
                }

                break;
            case BattlePetTeamLevel: // 34
                for (var slot : referencePlayer.getSession().getBattlePetMgr().getSlots()) {
                    if (slot.pet.level < reqValue) {
                        return false;
                    }
                }

                break;
            case PlayerIsNotInParty: // 35
                if (referencePlayer.getGroup()) {
                    return false;
                }

                break;
            case PlayerIsInParty: // 36
                if (!referencePlayer.getGroup()) {
                    return false;
                }

                break;
            case HasPersonalRatingEqualOrGreaterThan: // 37
                if (referencePlayer.getMaxPersonalArenaRatingRequirement(0) < reqValue) {
                    return false;
                }

                break;
            case HasTitle: // 38
                if (!referencePlayer.hasTitle(reqValue)) {
                    return false;
                }

                break;
            case PlayerLevelEqual: // 39
                if (referencePlayer.getLevel() != reqValue) {
                    return false;
                }

                break;
            case TargetLevelEqual: // 40
                if (refe == null || refe.getLevelForTarget(referencePlayer) != reqValue) {
                    return false;
                }

                break;
            case PlayerIsInZone: // 41
            {
                var zoneId = referencePlayer.getAreaId();
                var areaEntry = CliDB.AreaTableStorage.get(zoneId);

                if (areaEntry != null) {
                    if (areaEntry.hasFlag(AreaFlags.unk9)) {
                        zoneId = areaEntry.ParentAreaID;
                    }
                }

                if (zoneId != reqValue) {
                    return false;
                }

                break;
            }
            case TargetIsInZone: // 42
            {
                if (!refe) {
                    return false;
                }

                var zoneId = refe.getAreaId();
                var areaEntry = CliDB.AreaTableStorage.get(zoneId);

                if (areaEntry != null) {
                    if (areaEntry.hasFlag(AreaFlags.unk9)) {
                        zoneId = areaEntry.ParentAreaID;
                    }
                }

                if (zoneId != reqValue) {
                    return false;
                }

                break;
            }
            case PlayerHealthBelowPercent: // 43
                if (referencePlayer.getHealthPct() > (float) reqValue) {
                    return false;
                }

                break;
            case PlayerHealthAbovePercent: // 44
                if (referencePlayer.getHealthPct() < (float) reqValue) {
                    return false;
                }

                break;
            case PlayerHealthEqualsPercent: // 45
                if (referencePlayer.getHealthPct() != (float) reqValue) {
                    return false;
                }

                break;
            case TargetHealthBelowPercent: // 46
                if (refe == null || !refe.isUnit() || refe.toUnit().getHealthPct() > reqValue) {
                    return false;
                }

                break;
            case TargetHealthAbovePercent: // 47
                if (!refe || !refe.isUnit() || refe.toUnit().getHealthPct() < reqValue) {
                    return false;
                }

                break;
            case TargetHealthEqualsPercent: // 48
                if (!refe || !refe.isUnit() || refe.toUnit().getHealthPct() != reqValue) {
                    return false;
                }

                break;
            case PlayerHealthBelowValue: // 49
                if (referencePlayer.getHealth() > reqValue) {
                    return false;
                }

                break;
            case PlayerHealthAboveValue: // 50
                if (referencePlayer.getHealth() < reqValue) {
                    return false;
                }

                break;
            case PlayerHealthEqualsValue: // 51
                if (referencePlayer.getHealth() != reqValue) {
                    return false;
                }

                break;
            case TargetHealthBelowValue: // 52
                if (!refe || !refe.isUnit() || refe.toUnit().getHealth() > reqValue) {
                    return false;
                }

                break;
            case TargetHealthAboveValue: // 53
                if (!refe || !refe.isUnit() || refe.toUnit().getHealth() < reqValue) {
                    return false;
                }

                break;
            case TargetHealthEqualsValue: // 54
                if (!refe || !refe.isUnit() || refe.toUnit().getHealth() != reqValue) {
                    return false;
                }

                break;
            case TargetIsPlayerAndMeetsCondition: // 55
            {
                if (refe == null || !refe.isPlayer()) {
                    return false;
                }

                var playerCondition = CliDB.PlayerConditionStorage.get(reqValue);

                if (playerCondition == null || !ConditionManager.isPlayerMeetingCondition(refe.toPlayer(), playerCondition)) {
                    return false;
                }

                break;
            }
            case PlayerHasMoreThanAchievementPoints: // 56
                if (referencePlayer.getAchievementPoints() <= reqValue) {
                    return false;
                }

                break;
            case PlayerInLfgDungeon: // 57
                if (ConditionManager.getPlayerConditionLfgValue(referencePlayer, PlayerConditionLfgStatus.InLFGDungeon) == 0) {
                    return false;
                }

                break;
            case PlayerInRandomLfgDungeon: // 58
                if (ConditionManager.getPlayerConditionLfgValue(referencePlayer, PlayerConditionLfgStatus.InLFGRandomDungeon) == 0) {
                    return false;
                }

                break;
            case PlayerInFirstRandomLfgDungeon: // 59
                if (ConditionManager.getPlayerConditionLfgValue(referencePlayer, PlayerConditionLfgStatus.InLFGFirstRandomDungeon) == 0) {
                    return false;
                }

                break;
            case PlayerInRankedArenaMatch: // 60
            {
                var bg = referencePlayer.getBattleground();

                if (bg == null || !bg.isArena() || !bg.isRated()) {
                    return false;
                }

                break;
            }
            case PlayerInGuildParty: // 61 NYI
                return false;
            case PlayerGuildReputationEqualOrGreaterThan: // 62
                if (referencePlayer.getReputationMgr().getReputation(1168) < reqValue) {
                    return false;
                }

                break;
            case PlayerInRatedBattleground: // 63
            {
                var bg = referencePlayer.getBattleground();

                if (bg == null || !bg.isBattleground() || !bg.isRated()) {
                    return false;
                }

                break;
            }
            case PlayerBattlegroundRatingEqualOrGreaterThan: // 64
                if (referencePlayer.getRbgPersonalRating() < reqValue) {
                    return false;
                }

                break;
            case ResearchProjectRarity: // 65 NYI
            case ResearchProjectBranch: // 66 NYI
                return false;
            case WorldStateExpression: // 67
                var worldStateExpression = CliDB.WorldStateExpressionStorage.get(reqValue);

                if (worldStateExpression != null) {
                    return ConditionManager.isPlayerMeetingExpression(referencePlayer, worldStateExpression);
                }

                return false;
            case DungeonDifficulty: // 68
                if (referencePlayer.getMap().getDifficultyID() != Difficulty.forValue((byte) reqValue)) {
                    return false;
                }

                break;
            case PlayerLevelEqualOrGreaterThan: // 69
                if (referencePlayer.getLevel() < reqValue) {
                    return false;
                }

                break;
            case TargetLevelEqualOrGreaterThan: // 70
                if (!refe || !refe.isUnit() || refe.toUnit().getLevel() < reqValue) {
                    return false;
                }

                break;
            case PlayerLevelEqualOrLessThan: // 71
                if (referencePlayer.getLevel() > reqValue) {
                    return false;
                }

                break;
            case TargetLevelEqualOrLessThan: // 72
                if (!refe || !refe.isUnit() || refe.toUnit().getLevel() > reqValue) {
                    return false;
                }

                break;
            case ModifierTree: // 73
                var nextModifierTree = global.getCriteriaMgr().getModifierTree(reqValue);

                if (nextModifierTree != null) {
                    return modifierTreeSatisfied(nextModifierTree, miscValue1, miscValue2, refe, referencePlayer);
                }

                return false;
            case PlayerScenario: // 74
            {
                var scenario = referencePlayer.getScenario();

                if (scenario == null || scenario.getEntry().id != reqValue) {
                    return false;
                }

                break;
            }
            case TillersReputationGreaterThan: // 75
                if (referencePlayer.getReputationMgr().getReputation(1272) < reqValue) {
                    return false;
                }

                break;
            case BattlePetAchievementPointsEqualOrGreaterThan: // 76
            {
                static short getRootAchievementCategory (AchievementRecord achievement)
                {
                    var category = (short) achievement.category;

                    do {
                        var categoryEntry = CliDB.AchievementCategoryStorage.get(category);

                        if ((categoryEntry == null ? null : categoryEntry.parent) == -1) {
                            break;
                        }

                        category = categoryEntry.parent;
                    } while (true);

                    return category;
                }

                int petAchievementPoints = 0;

                for (var achievementId : referencePlayer.getCompletedAchievementIds()) {
                    var achievement = CliDB.AchievementStorage.get(achievementId);

                    if (getRootAchievementCategory(achievement) == SharedConst.AchivementCategoryPetBattles) {
                        petAchievementPoints += achievement.points;
                    }
                }

                if (petAchievementPoints < reqValue) {
                    return false;
                }

                break;
            }
            case UniqueBattlePetsEqualOrGreaterThan: // 77
                if (referencePlayer.getSession().getBattlePetMgr().getPetUniqueSpeciesCount() < reqValue) {
                    return false;
                }

                break;
            case BattlePetType: // 78
            {
                var speciesEntry = CliDB.BattlePetSpeciesStorage.get(miscValue1);

                if ((speciesEntry == null ? null : speciesEntry.PetTypeEnum) != reqValue) {
                    return false;
                }

                break;
            }
            case BattlePetHealthPercentLessThan: // 79 NYI - use target battle pet here, the one we were just battling
                return false;
            case GuildGroupMemberCountEqualOrGreaterThan: // 80
            {
                int guildMemberCount = 0;
                var group = referencePlayer.getGroup();

                if (group != null) {
                    for (var itr = group.getFirstMember(); itr != null; itr = itr.next()) {
                        if (itr.getSource().getGuildId() == referencePlayer.getGuildId()) {
                            ++guildMemberCount;
                        }
                    }
                }

                if (guildMemberCount < reqValue) {
                    return false;
                }

                break;
            }
            case BattlePetOpponentCreatureId: // 81 NYI
                return false;
            case PlayerScenarioStep: // 82
            {
                var scenario = referencePlayer.getScenario();

                if (scenario == null) {
                    return false;
                }

                if (scenario.getStep().orderIndex != (reqValue - 1)) {
                    return false;
                }

                break;
            }
            case ChallengeModeMedal: // 83
                return false; // OBSOLETE
            case PlayerOnQuest: // 84
                if (referencePlayer.findQuestSlot(reqValue) == SharedConst.MaxQuestLogSize) {
                    return false;
                }

                break;
            case ExaltedWithFaction: // 85
                if (referencePlayer.getReputationMgr().getReputation(reqValue) < 42000) {
                    return false;
                }

                break;
            case EarnedAchievementOnAccount: // 86
            case EarnedAchievementOnPlayer: // 87
                if (!referencePlayer.hasAchieved(reqValue)) {
                    return false;
                }

                break;
            case OrderOfTheCloudSerpentReputationGreaterThan: // 88
                if (referencePlayer.getReputationMgr().getReputation(1271) < reqValue) {
                    return false;
                }

                break;
            case BattlePetQuality: // 89 NYI
            case BattlePetFightWasPVP: // 90 NYI
                return false;
            case BattlePetSpecies: // 91
                if (miscValue1 != reqValue) {
                    return false;
                }

                break;
            case ServerExpansionEqualOrGreaterThan: // 92
                if (ConfigMgr.GetDefaultValue("character.EnforceRaceAndClassExpansions", true) && WorldConfig.getIntValue(WorldCfg.expansion) < reqValue) {
                    return false;
                }

                break;
            case PlayerHasBattlePetJournalLock: // 93
                if (!referencePlayer.getSession().getBattlePetMgr().getHasJournalLock()) {
                    return false;
                }

                break;
            case FriendshipRepReactionIsMet: // 94
            {
                var friendshipRepReaction = CliDB.FriendshipRepReactionStorage.get(reqValue);

                if (friendshipRepReaction == null) {
                    return false;
                }

                var friendshipReputation = CliDB.FriendshipReputationStorage.get(friendshipRepReaction.FriendshipRepID);

                if (friendshipReputation == null) {
                    return false;
                }

                if (referencePlayer.getReputation((int) friendshipReputation.factionID) < friendshipRepReaction.ReactionThreshold) {
                    return false;
                }

                break;
            }
            case ReputationWithFactionIsEqualOrGreaterThan: // 95
                if (referencePlayer.getReputationMgr().getReputation(reqValue) < reqValue) {
                    return false;
                }

                break;
            case ItemClassAndSubclass: // 96
            {
                var item = global.getObjectMgr().getItemTemplate((int) miscValue1);

                if (item == null || item.getClass() != itemClass.forValue((byte) reqValue) || item.getSubClass() != secondaryAsset) {
                    return false;
                }

                break;
            }
            case PlayerGender: // 97
                if (referencePlayer.getGender().getValue() != reqValue) {
                    return false;
                }

                break;
            case PlayerNativeGender: // 98
                if (referencePlayer.getNativeGender() != gender.forValue((byte) reqValue)) {
                    return false;
                }

                break;
            case PlayerSkillEqualOrGreaterThan: // 99
                if (referencePlayer.getPureSkillValue(SkillType.forValue(reqValue)).getValue() < secondaryAsset) {
                    return false;
                }

                break;
            case PlayerLanguageSkillEqualOrGreaterThan: // 100
            {
                var languageDescs = global.getLanguageMgr().getLanguageDescById(language.forValue(reqValue));

                if (!languageDescs.Any(desc -> referencePlayer.getSkillValue(SkillType.forValue(desc.skillId)).getValue() >= secondaryAsset)) {
                    return false;
                }

                break;
            }
            case PlayerIsInNormalPhase: // 101
                if (!PhasingHandler.inDbPhaseShift(referencePlayer, 0, (short) 0, 0)) {
                    return false;
                }

                break;
            case PlayerIsInPhase: // 102
                if (!PhasingHandler.inDbPhaseShift(referencePlayer, 0, (short) reqValue, 0)) {
                    return false;
                }

                break;
            case PlayerIsInPhaseGroup: // 103
                if (!PhasingHandler.inDbPhaseShift(referencePlayer, 0, (short) 0, reqValue)) {
                    return false;
                }

                break;
            case PlayerKnowsSpell: // 104
                if (!referencePlayer.hasSpell(reqValue)) {
                    return false;
                }

                break;
            case PlayerHasItemQuantity: // 105
                if (referencePlayer.getItemCount(reqValue, false) < secondaryAsset) {
                    return false;
                }

                break;
            case PlayerExpansionLevelEqualOrGreaterThan: // 106
                if (referencePlayer.getSession().getExpansion().getValue() < expansion.forValue(reqValue)) {
                    return false;
                }

                break;
            case PlayerHasAuraWithLabel: // 107
                if (!referencePlayer.getAuraQuery().hasLabel(reqValue).getResults().Any()) {
                    return false;
                }

                break;
            case PlayersRealmWorldState: // 108
                if (global.getWorldStateMgr().getValue((int) reqValue, referencePlayer.getMap()) != secondaryAsset) {
                    return false;
                }

                break;
            case TimeBetween: // 109
            {
                var from = time.GetUnixTimeFromPackedTime(reqValue);
                var to = time.GetUnixTimeFromPackedTime((int) secondaryAsset);

                if (GameTime.getGameTime() < from || GameTime.getGameTime() > to) {
                    return false;
                }

                break;
            }
            case PlayerHasCompletedQuest: // 110
                var questBit = global.getDB2Mgr().GetQuestUniqueBitFlag(reqValue);

                if (questBit != 0) {
                    if ((referencePlayer.getActivePlayerData().questCompleted.get(((int) questBit - 1) >> 6) & (1 << (((int) questBit - 1) & 63))) == 0) {
                        return false;
                    }
                }

                break;
            case PlayerIsReadyToTurnInQuest: // 111
                if (referencePlayer.getQuestStatus(reqValue) != QuestStatus.Complete) {
                    return false;
                }

                break;
            case PlayerHasCompletedQuestObjective: // 112
            {
                var objective = global.getObjectMgr().getQuestObjective(reqValue);

                if (objective == null) {
                    return false;
                }

                var quest = global.getObjectMgr().getQuestTemplate(objective.questID);

                if (quest == null) {
                    return false;
                }

                var slot = referencePlayer.findQuestSlot(objective.questID);

                if (slot >= SharedConst.MaxQuestLogSize || referencePlayer.getQuestRewardStatus(objective.questID) || !referencePlayer.isQuestObjectiveComplete(slot, quest, objective)) {
                    return false;
                }

                break;
            }
            case PlayerHasExploredArea: // 113
            {
                var areaTable = CliDB.AreaTableStorage.get(reqValue);

                if (areaTable == null) {
                    return false;
                }

                if (areaTable.AreaBit <= 0) {
                    break; // success
                }

                var playerIndexOffset = areaTable.AreaBit / activePlayerData.EXPLOREDZONESBITS;

                if (playerIndexOffset >= PlayerConst.EXPLOREDZONESSIZE) {
                    break;
                }

                if ((referencePlayer.getActivePlayerData().exploredZones.get(playerIndexOffset) & (1 << (areaTable.AreaBit % activePlayerData.EXPLOREDZONESBITS))) == 0) {
                    return false;
                }

                break;
            }
            case PlayerHasItemQuantityIncludingBank: // 114
                if (referencePlayer.getItemCount(reqValue, true) < secondaryAsset) {
                    return false;
                }

                break;
            case Weather: // 115
                if (referencePlayer.getMap().getZoneWeather(referencePlayer.getZoneId()) != WeatherState.forValue(reqValue)) {
                    return false;
                }

                break;
            case PlayerFaction: // 116
            {
                var race = CliDB.ChrRacesStorage.get(referencePlayer.getRace());

                if (race == null) {
                    return false;
                }

                var faction = CliDB.FactionTemplateStorage.get(race.factionID);

                if (faction == null) {
                    return false;
                }

                var factionIndex = -1;

                if (faction.factionGroup.hasFlag((byte) FactionMasks.Horde.getValue())) {
                    factionIndex = 0;
                } else if (faction.factionGroup.hasFlag((byte) FactionMasks.Alliance.getValue())) {
                    factionIndex = 1;
                } else if (faction.factionGroup.hasFlag((byte) FactionMasks.player.getValue())) {
                    factionIndex = 0;
                }

                if (factionIndex != reqValue) {
                    return false;
                }

                break;
            }
            case LfgStatusEqual: // 117
                if (ConditionManager.getPlayerConditionLfgValue(referencePlayer, PlayerConditionLfgStatus.forValue(reqValue)) != secondaryAsset) {
                    return false;
                }

                break;
            case LFgStatusEqualOrGreaterThan: // 118
                if (ConditionManager.getPlayerConditionLfgValue(referencePlayer, PlayerConditionLfgStatus.forValue(reqValue)) < secondaryAsset) {
                    return false;
                }

                break;
            case PlayerHasCurrencyEqualOrGreaterThan: // 119
                if (!referencePlayer.hasCurrency(reqValue, (int) secondaryAsset)) {
                    return false;
                }

                break;
            case TargetThreatListSizeLessThan: // 120
            {
                if (refe == null) {
                    return false;
                }

                var unitRef = refe.toUnit();

                if (unitRef == null || !unitRef.getCanHaveThreatList()) {
                    return false;
                }

                if (unitRef.getThreatManager().getThreatListSize() >= reqValue) {
                    return false;
                }

                break;
            }
            case PlayerHasTrackedCurrencyEqualOrGreaterThan: // 121
                if (referencePlayer.getCurrencyTrackedQuantity(reqValue) < secondaryAsset) {
                    return false;
                }

                break;
            case PlayerMapInstanceType: // 122
                if ((int) referencePlayer.getMap().getEntry().instanceType.getValue() != reqValue) {
                    return false;
                }

                break;
            case PlayerInTimeWalkerInstance: // 123
                if (!referencePlayer.hasPlayerFlag(playerFlags.Timewalking)) {
                    return false;
                }

                break;
            case PvpSeasonIsActive: // 124
                if (!WorldConfig.getBoolValue(WorldCfg.ArenaSeasonInProgress)) {
                    return false;
                }

                break;
            case PvpSeason: // 125
                if (WorldConfig.getIntValue(WorldCfg.ArenaSeasonId) != reqValue) {
                    return false;
                }

                break;
            case GarrisonTierEqualOrGreaterThan: // 126
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null || garrison.getGarrisonType() != GarrisonType.forValue(secondaryAsset) || garrison.getSiteLevel().GarrLevel < reqValue) {
                    return false;
                }

                break;
            }
            case GarrisonFollowersWithLevelEqualOrGreaterThan: // 127
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null) {
                    return false;
                }

                var followerCount = garrison.countFollowers(follower ->
                {
                    var garrFollower = CliDB.GarrFollowerStorage.get(follower.packetInfo.garrFollowerID);

                    return (garrFollower == null ? null : garrFollower.garrFollowerTypeID) == tertiaryAsset && follower.packetInfo.followerLevel >= secondaryAsset;
                });

                if (followerCount < reqValue) {
                    return false;
                }

                break;
            }
            case GarrisonFollowersWithQualityEqualOrGreaterThan: // 128
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null) {
                    return false;
                }

                var followerCount = garrison.countFollowers(follower ->
                {
                    var garrFollower = CliDB.GarrFollowerStorage.get(follower.packetInfo.garrFollowerID);

                    return (garrFollower == null ? null : garrFollower.garrFollowerTypeID) == tertiaryAsset && follower.packetInfo.quality >= secondaryAsset;
                });

                if (followerCount < reqValue) {
                    return false;
                }

                break;
            }
            case GarrisonFollowerWithAbilityAtLevelEqualOrGreaterThan: // 129
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null) {
                    return false;
                }

                var followerCount = garrison.countFollowers(follower ->
                {
                    var garrFollower = CliDB.GarrFollowerStorage.get(follower.packetInfo.garrFollowerID);

                    return (garrFollower == null ? null : garrFollower.garrFollowerTypeID) == tertiaryAsset && follower.packetInfo.followerLevel >= reqValue && follower.hasAbility((int) secondaryAsset);
                });

                if (followerCount < 1) {
                    return false;
                }

                break;
            }
            case GarrisonFollowerWithTraitAtLevelEqualOrGreaterThan: // 130
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null) {
                    return false;
                }

                var traitEntry = CliDB.GarrAbilityStorage.get(secondaryAsset);

                if (traitEntry == null || !traitEntry.flags.hasFlag(GarrisonAbilityFlags.Trait)) {
                    return false;
                }

                var followerCount = garrison.countFollowers(follower ->
                {
                    var garrFollower = CliDB.GarrFollowerStorage.get(follower.packetInfo.garrFollowerID);

                    return (garrFollower == null ? null : garrFollower.garrFollowerTypeID) == tertiaryAsset && follower.packetInfo.followerLevel >= reqValue && follower.hasAbility((int) secondaryAsset);
                });

                if (followerCount < 1) {
                    return false;
                }

                break;
            }
            case GarrisonFollowerWithAbilityAssignedToBuilding: // 131
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null || garrison.getGarrisonType() != GarrisonType.forValue(tertiaryAsset)) {
                    return false;
                }

                var followerCount = garrison.countFollowers(follower ->
                {
                    var followerBuilding = CliDB.GarrBuildingStorage.get(follower.packetInfo.currentBuildingID);

                    if (followerBuilding == null) {
                        return false;
                    }

                    return followerBuilding.BuildingType == secondaryAsset && follower.hasAbility(reqValue);

                    ;
                });

                if (followerCount < 1) {
                    return false;
                }

                break;
            }
            case GarrisonFollowerWithTraitAssignedToBuilding: // 132
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null || garrison.getGarrisonType() != GarrisonType.forValue(tertiaryAsset)) {
                    return false;
                }

                var traitEntry = CliDB.GarrAbilityStorage.get(reqValue);

                if (traitEntry == null || !traitEntry.flags.hasFlag(GarrisonAbilityFlags.Trait)) {
                    return false;
                }

                var followerCount = garrison.countFollowers(follower ->
                {
                    var followerBuilding = CliDB.GarrBuildingStorage.get(follower.packetInfo.currentBuildingID);

                    if (followerBuilding == null) {
                        return false;
                    }

                    return followerBuilding.BuildingType == secondaryAsset && follower.hasAbility(reqValue);

                    ;
                });

                if (followerCount < 1) {
                    return false;
                }

                break;
            }
            case GarrisonFollowerWithLevelAssignedToBuilding: // 133
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null || garrison.getGarrisonType() != GarrisonType.forValue(tertiaryAsset)) {
                    return false;
                }

                var followerCount = garrison.countFollowers(follower ->
                {
                    if (follower.packetInfo.followerLevel < reqValue) {
                        return false;
                    }

                    var followerBuilding = CliDB.GarrBuildingStorage.get(follower.packetInfo.currentBuildingID);

                    if (followerBuilding == null) {
                        return false;
                    }

                    return followerBuilding.BuildingType == secondaryAsset;
                });

                if (followerCount < 1) {
                    return false;
                }

                break;
            }
            case GarrisonBuildingWithLevelEqualOrGreaterThan: // 134
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null || garrison.getGarrisonType() != GarrisonType.forValue(tertiaryAsset)) {
                    return false;
                }

                for (var plot : garrison.getPlots()) {
                    if (plot.buildingInfo.packetInfo == null) {
                        continue;
                    }

                    var building = CliDB.GarrBuildingStorage.get(plot.buildingInfo.packetInfo.garrBuildingID);

                    if (building == null || building.UpgradeLevel < reqValue || building.BuildingType != secondaryAsset) {
                        continue;
                    }

                    return true;
                }

                return false;
            }
            case HasBlueprintForGarrisonBuilding: // 135
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null || garrison.getGarrisonType() != GarrisonType.forValue(secondaryAsset)) {
                    return false;
                }

                if (!garrison.hasBlueprint(reqValue)) {
                    return false;
                }

                break;
            }
            case HasGarrisonBuildingSpecialization: // 136
                return false; // OBSOLETE
            case AllGarrisonPlotsAreFull: // 137
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null || garrison.getGarrisonType() != GarrisonType.forValue(reqValue)) {
                    return false;
                }

                for (var plot : garrison.getPlots()) {
                    if (plot.buildingInfo.packetInfo == null) {
                        return false;
                    }
                }

                break;
            }
            case PlayerIsInOwnGarrison: // 138
                if (!referencePlayer.getMap().isGarrison() || referencePlayer.getMap().getInstanceId() != referencePlayer.getGUID().getCounter()) {
                    return false;
                }

                break;
            case GarrisonShipmentOfTypeIsPending: // 139 NYI
                return false;
            case GarrisonBuildingIsUnderConstruction: // 140
            {
                var building = CliDB.GarrBuildingStorage.get(reqValue);

                if (building == null) {
                    return false;
                }

                var garrison = referencePlayer.getGarrison();

                if (garrison == null || garrison.getGarrisonType() != GarrisonType.forValue(tertiaryAsset)) {
                    return false;
                }

                for (var plot : garrison.getPlots()) {
                    if (plot.buildingInfo.packetInfo == null || plot.buildingInfo.packetInfo.garrBuildingID != reqValue) {
                        continue;
                    }

                    return !plot.buildingInfo.packetInfo.active;
                }

                return false;
            }
            case GarrisonMissionHasBeenCompleted: // 141 NYI
                return true;
            case GarrisonBuildingLevelEqual: // 142
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null || garrison.getGarrisonType() != GarrisonType.forValue(tertiaryAsset)) {
                    return false;
                }

                for (var plot : garrison.getPlots()) {
                    if (plot.buildingInfo.packetInfo == null) {
                        continue;
                    }

                    var building = CliDB.GarrBuildingStorage.get(plot.buildingInfo.packetInfo.garrBuildingID);

                    if (building == null || building.UpgradeLevel != secondaryAsset || building.BuildingType != reqValue) {
                        continue;
                    }

                    return true;
                }

                return false;
            }
            case GarrisonFollowerHasAbility: // 143
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null || garrison.getGarrisonType() != GarrisonType.forValue(secondaryAsset)) {
                    return false;
                }

                if (miscValue1 != 0) {
                    var follower = garrison.getFollower(miscValue1);

                    if (follower == null) {
                        return false;
                    }

                    if (!follower.hasAbility(reqValue)) {
                        return false;
                    }
                } else {
                    var followerCount = garrison.countFollowers(follower ->
                    {
                        return follower.hasAbility(reqValue);
                    });

                    if (followerCount < 1) {
                        return false;
                    }
                }

                break;
            }
            case GarrisonFollowerHasTrait: // 144
            {
                var traitEntry = CliDB.GarrAbilityStorage.get(reqValue);

                if (traitEntry == null || !traitEntry.flags.hasFlag(GarrisonAbilityFlags.Trait)) {
                    return false;
                }

                var garrison = referencePlayer.getGarrison();

                if (garrison == null || garrison.getGarrisonType() != GarrisonType.forValue(secondaryAsset)) {
                    return false;
                }

                if (miscValue1 != 0) {
                    var follower = garrison.getFollower(miscValue1);

                    if (follower == null || !follower.hasAbility(reqValue)) {
                        return false;
                    }
                } else {
                    var followerCount = garrison.countFollowers(follower ->
                    {
                        return follower.hasAbility(reqValue);
                    });

                    if (followerCount < 1) {
                        return false;
                    }
                }

                break;
            }
            case GarrisonFollowerQualityEqual: // 145
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null || garrison.getGarrisonType() != GarrisonType.Garrison) {
                    return false;
                }

                if (miscValue1 != 0) {
                    var follower = garrison.getFollower(miscValue1);

                    if (follower == null || follower.packetInfo.quality < reqValue) {
                        return false;
                    }
                } else {
                    var followerCount = garrison.countFollowers(follower ->
                    {
                        return follower.packetInfo.quality >= reqValue;
                    });

                    if (followerCount < 1) {
                        return false;
                    }
                }

                break;
            }
            case GarrisonFollowerLevelEqual: // 146
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null || garrison.getGarrisonType() != GarrisonType.forValue(secondaryAsset)) {
                    return false;
                }

                if (miscValue1 != 0) {
                    var follower = garrison.getFollower(miscValue1);

                    if (follower == null || follower.packetInfo.followerLevel != reqValue) {
                        return false;
                    }
                } else {
                    var followerCount = garrison.countFollowers(follower ->
                    {
                        return follower.packetInfo.followerLevel == reqValue;
                    });

                    if (followerCount < 1) {
                        return false;
                    }
                }

                break;
            }
            case GarrisonMissionIsRare: // 147 NYI
            case GarrisonMissionIsElite: // 148 NYI
                return false;
            case CurrentGarrisonBuildingLevelEqual: // 149
            {
                if (miscValue1 == 0) {
                    return false;
                }

                var garrison = referencePlayer.getGarrison();

                if (garrison == null) {
                    return false;
                }

                for (var plot : garrison.getPlots()) {
                    if (plot.buildingInfo.packetInfo == null || plot.buildingInfo.packetInfo.garrBuildingID != miscValue1) {
                        continue;
                    }

                    var building = CliDB.GarrBuildingStorage.get(plot.buildingInfo.packetInfo.garrBuildingID);

                    if (building == null || building.UpgradeLevel != reqValue) {
                        continue;
                    }

                    return true;
                }

                break;
            }
            case GarrisonPlotInstanceHasBuildingThatIsReadyToActivate: // 150
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null) {
                    return false;
                }

                var plot = garrison.getPlot(reqValue);

                if (plot == null) {
                    return false;
                }

                if (!plot.buildingInfo.canActivate() || plot.buildingInfo.packetInfo == null || plot.buildingInfo.packetInfo.active) {
                    return false;
                }

                break;
            }
            case BattlePetTeamWithSpeciesEqualOrGreaterThan: // 151
            {
                int count = 0;

                for (var slot : referencePlayer.getSession().getBattlePetMgr().getSlots()) {
                    if (slot.pet.species == secondaryAsset) {
                        ++count;
                    }
                }

                if (count < reqValue) {
                    return false;
                }

                break;
            }
            case BattlePetTeamWithTypeEqualOrGreaterThan: // 152
            {
                int count = 0;

                for (var slot : referencePlayer.getSession().getBattlePetMgr().getSlots()) {
                    var species = CliDB.BattlePetSpeciesStorage.get(slot.pet.species);

                    if (species != null) {
                        if (species.PetTypeEnum == secondaryAsset) {
                            ++count;
                        }
                    }
                }

                if (count < reqValue) {
                    return false;
                }

                break;
            }
            case PetBattleLastAbility: // 153 NYI
            case PetBattleLastAbilityType: // 154 NYI
                return false;
            case BattlePetTeamWithAliveEqualOrGreaterThan: // 155
            {
                int count = 0;

                for (var slot : referencePlayer.getSession().getBattlePetMgr().getSlots()) {
                    if (slot.pet.health > 0) {
                        ++count;
                    }
                }

                if (count < reqValue) {
                    return false;
                }

                break;
            }
            case HasGarrisonBuildingActiveSpecialization: // 156
                return false; // OBSOLETE
            case HasGarrisonFollower: // 157
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null) {
                    return false;
                }

                var followerCount = garrison.countFollowers(follower ->
                {
                    return follower.packetInfo.garrFollowerID == reqValue;
                });

                if (followerCount < 1) {
                    return false;
                }

                break;
            }
            case PlayerQuestObjectiveProgressEqual: // 158
            {
                var objective = global.getObjectMgr().getQuestObjective(reqValue);

                if (objective == null) {
                    return false;
                }

                if (referencePlayer.getQuestObjectiveData(objective) != secondaryAsset) {
                    return false;
                }

                break;
            }
            case PlayerQuestObjectiveProgressEqualOrGreaterThan: // 159
            {
                var objective = global.getObjectMgr().getQuestObjective(reqValue);

                if (objective == null) {
                    return false;
                }

                if (referencePlayer.getQuestObjectiveData(objective) < secondaryAsset) {
                    return false;
                }

                break;
            }
            case IsPTRRealm: // 160
            case IsBetaRealm: // 161
            case IsQARealm: // 162
                return false; // always false
            case GarrisonShipmentContainerIsFull: // 163
                return false;
            case PlayerCountIsValidToStartGarrisonInvasion: // 164
                return true; // Only 1 player is required and referencePlayer.getMap() will ALWAYS have at least the referencePlayer on it
            case InstancePlayerCountEqualOrLessThan: // 165
                if (referencePlayer.getMap().getPlayersCountExceptGMs() > reqValue) {
                    return false;
                }

                break;
            case AllGarrisonPlotsFilledWithBuildingsWithLevelEqualOrGreater: // 166
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null || garrison.getGarrisonType() != GarrisonType.forValue(reqValue)) {
                    return false;
                }

                for (var plot : garrison.getPlots()) {
                    if (plot.buildingInfo.packetInfo == null) {
                        return false;
                    }

                    var building = CliDB.GarrBuildingStorage.get(plot.buildingInfo.packetInfo.garrBuildingID);

                    if (building == null || building.UpgradeLevel != reqValue) {
                        return false;
                    }
                }

                break;
            }
            case GarrisonMissionType: // 167 NYI
                return false;
            case GarrisonFollowerItemLevelEqualOrGreaterThan: // 168
            {
                if (miscValue1 == 0) {
                    return false;
                }

                var garrison = referencePlayer.getGarrison();

                if (garrison == null) {
                    return false;
                }

                var followerCount = garrison.countFollowers(follower ->
                {
                    return follower.packetInfo.garrFollowerID == miscValue1 && follower.getItemLevel() >= reqValue;
                });

                if (followerCount < 1) {
                    return false;
                }

                break;
            }
            case GarrisonFollowerCountWithItemLevelEqualOrGreaterThan: // 169
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null) {
                    return false;
                }

                var followerCount = garrison.countFollowers(follower ->
                {
                    var garrFollower = CliDB.GarrFollowerStorage.get(follower.packetInfo.garrFollowerID);

                    return (garrFollower == null ? null : garrFollower.garrFollowerTypeID) == tertiaryAsset && follower.getItemLevel() >= secondaryAsset;
                });

                if (followerCount < reqValue) {
                    return false;
                }

                break;
            }
            case GarrisonTierEqual: // 170
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null || garrison.getGarrisonType() != GarrisonType.forValue(secondaryAsset) || garrison.getSiteLevel().GarrLevel != reqValue) {
                    return false;
                }

                break;
            }
            case InstancePlayerCountEqual: // 171
                if (referencePlayer.getMap().getPlayers().size() != reqValue) {
                    return false;
                }

                break;
            case CurrencyId: // 172
                if (miscValue1 != reqValue) {
                    return false;
                }

                break;
            case SelectionIsPlayerCorpse: // 173
                if (referencePlayer.getTarget().getHigh() != HighGuid.Corpse) {
                    return false;
                }

                break;
            case PlayerCanAcceptQuest: // 174
            {
                var quest = global.getObjectMgr().getQuestTemplate(reqValue);

                if (quest == null) {
                    return false;
                }

                if (!referencePlayer.canTakeQuest(quest, false)) {
                    return false;
                }

                break;
            }
            case GarrisonFollowerCountWithLevelEqualOrGreaterThan: // 175
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null || garrison.getGarrisonType() != GarrisonType.forValue(tertiaryAsset)) {
                    return false;
                }

                var followerCount = garrison.countFollowers(follower ->
                {
                    var garrFollower = CliDB.GarrFollowerStorage.get(follower.packetInfo.garrFollowerID);

                    return (garrFollower == null ? null : garrFollower.garrFollowerTypeID) == tertiaryAsset && follower.packetInfo.followerLevel == secondaryAsset;
                });

                if (followerCount < reqValue) {
                    return false;
                }

                break;
            }
            case GarrisonFollowerIsInBuilding: // 176
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null) {
                    return false;
                }

                var followerCount = garrison.countFollowers(follower ->
                {
                    return follower.packetInfo.garrFollowerID == reqValue && follower.packetInfo.currentBuildingID == secondaryAsset;
                });

                if (followerCount < 1) {
                    return false;
                }

                break;
            }
            case GarrisonMissionCountLessThan: // 177 NYI
                return false;
            case GarrisonPlotInstanceCountEqualOrGreaterThan: // 178
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null || garrison.getGarrisonType() != GarrisonType.forValue(reqValue)) {
                    return false;
                }

                int plotCount = 0;

                for (var plot : garrison.getPlots()) {
                    var garrPlotInstance = CliDB.GarrPlotInstanceStorage.get(plot.packetInfo.garrPlotInstanceID);

                    if (garrPlotInstance == null || garrPlotInstance.GarrPlotID != secondaryAsset) {
                        continue;
                    }

                    ++plotCount;
                }

                if (plotCount < reqValue) {
                    return false;
                }

                break;
            }
            case CurrencySource: // 179 NYI
                return false;
            case PlayerIsInNotOwnGarrison: // 180
                if (!referencePlayer.getMap().isGarrison() || referencePlayer.getMap().getInstanceId() == referencePlayer.getGUID().getCounter()) {
                    return false;
                }

                break;
            case HasActiveGarrisonFollower: // 181
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null) {
                    return false;
                }

                var followerCount = garrison.countFollowers(follower -> follower.packetInfo.garrFollowerID == reqValue && (follower.packetInfo.followerStatus & (byte) GarrisonFollowerStatus.inactive.getValue()) == 0);

                if (followerCount < 1) {
                    return false;
                }

                break;
            }
            case PlayerDailyRandomValueMod_X_Equals: // 182 NYI
                return false;
            case PlayerHasMount: // 183
            {
                for (var pair : referencePlayer.getSession().getCollectionMgr().getAccountMounts().entrySet()) {
                    var mount = global.getDB2Mgr().GetMount(pair.getKey());

                    if (mount == null) {
                        continue;
                    }

                    if (mount.id == reqValue) {
                        return true;
                    }
                }

                return false;
            }
            case GarrisonFollowerCountWithInactiveWithItemLevelEqualOrGreaterThan: // 184
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null) {
                    return false;
                }

                var followerCount = garrison.countFollowers(follower ->
                {
                    var garrFollower = CliDB.GarrFollowerStorage.get(follower.packetInfo.garrFollowerID);

                    if (garrFollower == null) {
                        return false;
                    }

                    return follower.getItemLevel() >= secondaryAsset && garrFollower.garrFollowerTypeID == tertiaryAsset;
                });

                if (followerCount < reqValue) {
                    return false;
                }

                break;
            }
            case GarrisonFollowerIsOnAMission: // 185
            {
                var garrison = referencePlayer.getGarrison();

                if (garrison == null) {
                    return false;
                }

                var followerCount = garrison.countFollowers(follower -> follower.packetInfo.garrFollowerID == reqValue && follower.packetInfo.currentMissionID != 0);

                if (followerCount < 1) {
                    return false;
                }

                break;
            }
            case GarrisonMissionCountInSetLessThan: // 186 NYI
                return false;
            case GarrisonFollowerType: // 187
            {
                var garrFollower = CliDB.GarrFollowerStorage.get(miscValue1);

                if (garrFollower == null || garrFollower.garrFollowerTypeID != reqValue) {
                    return false;
                }

                break;
            }
            case PlayerUsedBoostLessThanHoursAgoRealTime: // 188 NYI
            case PlayerUsedBoostLessThanHoursAgoGameTime: // 189 NYI
                return false;
            case PlayerIsMercenary: // 190
                if (!referencePlayer.hasPlayerFlagEx(playerFlagsEx.MercenaryMode)) {
                    return false;
                }

                break;
            case PlayerEffectiveRace: // 191 NYI
            case TargetEffectiveRace: // 192 NYI
                return false;
            case HonorLevelEqualOrGreaterThan: // 193
                if (referencePlayer.getHonorLevel() < reqValue) {
                    return false;
                }

                break;
            case PrestigeLevelEqualOrGreaterThan: // 194
                return false; // OBSOLOTE
            case GarrisonMissionIsReadyToCollect: // 195 NYI
            case PlayerIsInstanceOwner: // 196 NYI
                return false;
            case PlayerHasHeirloom: // 197
                if (!referencePlayer.getSession().getCollectionMgr().getAccountHeirlooms().containsKey(reqValue)) {
                    return false;
                }

                break;
            case TeamPoints: // 198 NYI
                return false;
            case PlayerHasToy: // 199
                if (!referencePlayer.getSession().getCollectionMgr().hasToy(reqValue)) {
                    return false;
                }

                break;
            case PlayerHasTransmog: // 200
            {

                var(PermAppearance, TempAppearance) = referencePlayer.session.CollectionMgr.hasItemAppearance(reqValue);

                if (!PermAppearance || TempAppearance) {
                    return false;
                }

                break;
            }
            case GarrisonTalentSelected: // 201 NYI
            case GarrisonTalentResearched: // 202 NYI
                return false;
            case PlayerHasRestriction: // 203
            {
                var restrictionIndex = referencePlayer.getActivePlayerData().characterRestrictions.FindIndexIf(restriction -> restriction.type == reqValue);

                if (restrictionIndex < 0) {
                    return false;
                }

                break;
            }
            case PlayerCreatedCharacterLessThanHoursAgoRealTime: // 204 NYI
                return false;
            case PlayerCreatedCharacterLessThanHoursAgoGameTime: // 205
                if (duration.FromHours(reqValue) >= duration.FromSeconds(referencePlayer.getTotalPlayedTime())) {
                    return false;
                }

                break;
            case QuestHasQuestInfoId: // 206
            {
                var quest = global.getObjectMgr().getQuestTemplate((int) miscValue1);

                if (quest == null || quest.id != reqValue) {
                    return false;
                }

                break;
            }
            case GarrisonTalentResearchInProgress: // 207 NYI
                return false;
            case PlayerEquippedArtifactAppearanceSet: // 208
            {
                var artifactAura = referencePlayer.getAura(PlayerConst.ArtifactsAllWeaponsGeneralWeaponEquippedPassive);

                if (artifactAura != null) {
                    var artifact = referencePlayer.getItemByGuid(artifactAura.castItemGuid);

                    if (artifact != null) {
                        var artifactAppearance = CliDB.ArtifactAppearanceStorage.get(artifact.getModifier(ItemModifier.artifactAppearanceId));

                        if (artifactAppearance != null) {
                            if (artifactAppearance.ArtifactAppearanceSetID == reqValue) {
                                break;
                            }
                        }
                    }
                }

                return false;
            }
            case PlayerHasCurrencyEqual: // 209
                if (referencePlayer.getCurrencyQuantity(reqValue) != secondaryAsset) {
                    return false;
                }

                break;
            case MinimumAverageItemHighWaterMarkForSpec: // 210 NYI
                return false;
            case PlayerScenarioType: // 211
            {
                var scenario = referencePlayer.getScenario();

                if (scenario == null) {
                    return false;
                }

                if (scenario.getEntry().type != reqValue) {
                    return false;
                }

                break;
            }
            case PlayersAuthExpansionLevelEqualOrGreaterThan: // 212
                if (referencePlayer.getSession().getAccountExpansion().getValue() < expansion.forValue(reqValue)) {
                    return false;
                }

                break;
            case PlayerLastWeek2v2Rating: // 213 NYI
            case PlayerLastWeek3v3Rating: // 214 NYI
            case PlayerLastWeekRBGRating: // 215 NYI
                return false;
            case GroupMemberCountFromConnectedRealmEqualOrGreaterThan: // 216
            {
                int memberCount = 0;
                var group = referencePlayer.getGroup();

                if (group != null) {
                    for (var itr = group.getFirstMember(); itr != null; itr = itr.next()) {
                        if (itr.getSource() != referencePlayer && referencePlayer.getPlayerData().virtualPlayerRealm == itr.getSource().getPlayerData().virtualPlayerRealm) {
                            ++memberCount;
                        }
                    }
                }

                if (memberCount < reqValue) {
                    return false;
                }

                break;
            }
            case ArtifactTraitUnlockedCountEqualOrGreaterThan: // 217
            {
                var artifact = referencePlayer.getItemByEntry((int) secondaryAsset, ItemSearchLocation.Everywhere);

                if (artifact == null) {
                    return false;
                }

                if (artifact.getTotalUnlockedArtifactPowers() < reqValue) {
                    return false;
                }

                break;
            }
            case ParagonReputationLevelEqualOrGreaterThan: // 218
                if (referencePlayer.getReputationMgr().getParagonLevel((int) miscValue1) < reqValue) {
                    return false;
                }

                return false;
            case GarrisonShipmentIsReady: // 219 NYI
                return false;
            case PlayerIsInPvpBrawl: // 220
            {
                var bg = CliDB.BattlemasterListStorage.get(referencePlayer.getBattlegroundTypeId());

                if (bg == null || !bg.flags.hasFlag(BattlemasterListFlags.Brawl)) {
                    return false;
                }

                break;
            }
            case ParagonReputationLevelWithFactionEqualOrGreaterThan: // 221
            {
                var faction = CliDB.FactionStorage.get(secondaryAsset);

                if (faction == null) {
                    return false;
                }

                if (referencePlayer.getReputationMgr().getParagonLevel(faction.ParagonFactionID) < reqValue) {
                    return false;
                }

                break;
            }
            case PlayerHasItemWithBonusListFromTreeAndQuality: // 222
            {
                var bonusListIDs = global.getDB2Mgr().GetAllItemBonusTreeBonuses(reqValue);

                if (bonusListIDs.isEmpty()) {
                    return false;
                }

                var bagScanReachedEnd = referencePlayer.forEachItem(ItemSearchLocation.Everywhere, item ->
                {
                    var hasBonus = item.getBonusListIDs().Any(bonusListID -> bonusListIDs.contains(bonusListID));

                    return !hasBonus;
                });

                if (bagScanReachedEnd) {
                    return false;
                }

                break;
            }
            case PlayerHasEmptyInventorySlotCountEqualOrGreaterThan: // 223
                if (referencePlayer.getFreeInventorySlotCount(ItemSearchLocation.Inventory) < reqValue) {
                    return false;
                }

                break;
            case PlayerHasItemInHistoryOfProgressiveEvent: // 224 NYI
                return false;
            case PlayerHasArtifactPowerRankCountPurchasedEqualOrGreaterThan: // 225
            {
                var artifactAura = referencePlayer.getAura(PlayerConst.ArtifactsAllWeaponsGeneralWeaponEquippedPassive);

                if (artifactAura == null) {
                    return false;
                }

                var artifact = referencePlayer.getItemByGuid(artifactAura.castItemGuid);

                if (!artifact) {
                    return false;
                }

                var artifactPower = artifact.getArtifactPower((int) secondaryAsset);

                if (artifactPower == null) {
                    return false;
                }

                if (artifactPower.purchasedRank < reqValue) {
                    return false;
                }

                break;
            }
            case PlayerHasBoosted: // 226
                if (referencePlayer.hasLevelBoosted()) {
                    return false;
                }

                break;
            case PlayerHasRaceChanged: // 227
                if (referencePlayer.hasRaceChanged()) {
                    return false;
                }

                break;
            case PlayerHasBeenGrantedLevelsFromRaF: // 228
                if (referencePlayer.hasBeenGrantedLevelsFromRaF()) {
                    return false;
                }

                break;
            case IsTournamentRealm: // 229
                return false;
            case PlayerCanAccessAlliedRaces: // 230
                if (!referencePlayer.getSession().canAccessAlliedRaces()) {
                    return false;
                }

                break;
            case GroupMemberCountWithAchievementEqualOrLessThan: // 231
            {
                var group = referencePlayer.getGroup();

                if (group != null) {
                    int membersWithAchievement = 0;

                    for (var itr = group.getFirstMember(); itr != null; itr = itr.next()) {
                        if (itr.getSource().hasAchieved((int) secondaryAsset)) {
                            ++membersWithAchievement;
                        }
                    }

                    if (membersWithAchievement > reqValue) {
                        return false;
                    }
                }

                // true if no group
                break;
            }
            case PlayerMainhandWeaponType: // 232
            {
                var visibleItem = referencePlayer.getPlayerData().visibleItems.get(EquipmentSlot.MainHand);
                var itemSubclass = (int) ItemSubClassWeapon.Fist.getValue();
                var itemTemplate = global.getObjectMgr().getItemTemplate(visibleItem.itemID);

                if (itemTemplate != null) {
                    if (itemTemplate.getClass() == itemClass.Weapon) {
                        itemSubclass = itemTemplate.getSubClass();

                        var itemModifiedAppearance = global.getDB2Mgr().getItemModifiedAppearance(visibleItem.itemID, visibleItem.itemAppearanceModID);

                        if (itemModifiedAppearance != null) {
                            var itemModifiedAppearaceExtra = CliDB.ItemModifiedAppearanceExtraStorage.get(itemModifiedAppearance.id);

                            if (itemModifiedAppearaceExtra != null) {
                                if (itemModifiedAppearaceExtra.DisplayWeaponSubclassID > 0) {
                                    itemSubclass = (int) itemModifiedAppearaceExtra.DisplayWeaponSubclassID;
                                }
                            }
                        }
                    }
                }

                if (itemSubclass != reqValue) {
                    return false;
                }

                break;
            }
            case PlayerOffhandWeaponType: // 233
            {
                var visibleItem = referencePlayer.getPlayerData().visibleItems.get(EquipmentSlot.OffHand);
                var itemSubclass = (int) ItemSubClassWeapon.Fist.getValue();
                var itemTemplate = global.getObjectMgr().getItemTemplate(visibleItem.itemID);

                if (itemTemplate != null) {
                    if (itemTemplate.getClass() == itemClass.Weapon) {
                        itemSubclass = itemTemplate.getSubClass();

                        var itemModifiedAppearance = global.getDB2Mgr().getItemModifiedAppearance(visibleItem.itemID, visibleItem.itemAppearanceModID);

                        if (itemModifiedAppearance != null) {
                            var itemModifiedAppearaceExtra = CliDB.ItemModifiedAppearanceExtraStorage.get(itemModifiedAppearance.id);

                            if (itemModifiedAppearaceExtra != null) {
                                if (itemModifiedAppearaceExtra.DisplayWeaponSubclassID > 0) {
                                    itemSubclass = (int) itemModifiedAppearaceExtra.DisplayWeaponSubclassID;
                                }
                            }
                        }
                    }
                }

                if (itemSubclass != reqValue) {
                    return false;
                }

                break;
            }
            case PlayerPvpTier: // 234
            {
                var pvpTier = CliDB.PvpTierStorage.get(reqValue);

                if (pvpTier == null) {
                    return false;
                }

                var pvpInfo = referencePlayer.getPvpInfoForBracket((byte) pvpTier.BracketID);

                if (pvpInfo == null) {
                    return false;
                }

                if (pvpTier.id != pvpInfo.pvpTierID) {
                    return false;
                }

                break;
            }
            case PlayerAzeriteLevelEqualOrGreaterThan: // 235
            {
                var heartOfAzeroth = referencePlayer.getItemByEntry(PlayerConst.ItemIdHeartOfAzeroth, ItemSearchLocation.Everywhere);

                if (!heartOfAzeroth || heartOfAzeroth.getAsAzeriteItem().getLevel() < reqValue) {
                    return false;
                }

                break;
            }
            case PlayerIsOnQuestInQuestline: // 236
            {
                var isOnQuest = false;
                var questLineQuests = global.getDB2Mgr().GetQuestsForQuestLine(reqValue);

                if (!questLineQuests.isEmpty()) {
                    isOnQuest = questLineQuests.Any(questLineQuest -> referencePlayer.findQuestSlot(questLineQuest.questID) < SharedConst.MaxQuestLogSize);
                }

                if (!isOnQuest) {
                    return false;
                }

                break;
            }
            case PlayerIsQnQuestLinkedToScheduledWorldStateGroup: // 237
                return false; // OBSOLETE (db2 removed)
            case PlayerIsInRaidGroup: // 238
            {
                var group = referencePlayer.getGroup();

                if (group == null || !group.isRaidGroup()) {
                    return false;
                }

                break;
            }
            case PlayerPvpTierInBracketEqualOrGreaterThan: // 239
            {
                var pvpInfo = referencePlayer.getPvpInfoForBracket((byte) secondaryAsset);

                if (pvpInfo == null) {
                    return false;
                }

                var pvpTier = CliDB.PvpTierStorage.get(pvpInfo.pvpTierID);

                if (pvpTier == null) {
                    return false;
                }

                if (pvpTier.rank < reqValue) {
                    return false;
                }

                break;
            }
            case PlayerCanAcceptQuestInQuestline: // 240
            {
                var questLineQuests = global.getDB2Mgr().GetQuestsForQuestLine(reqValue);

                if (questLineQuests.isEmpty()) {
                    return false;
                }

                var canTakeQuest = questLineQuests.Any(questLineQuest ->
                {
                    var quest = global.getObjectMgr().getQuestTemplate(questLineQuest.questID);

                    if (quest != null) {
                        return referencePlayer.canTakeQuest(quest, false);
                    }

                    return false;
                });

                if (!canTakeQuest) {
                    return false;
                }

                break;
            }
            case PlayerHasCompletedQuestline: // 241
            {
                var questLineQuests = global.getDB2Mgr().GetQuestsForQuestLine(reqValue);

                if (questLineQuests.isEmpty()) {
                    return false;
                }

                for (var questLineQuest : questLineQuests) {
                    if (!referencePlayer.getQuestRewardStatus(questLineQuest.questID)) {
                        return false;
                    }
                }

                break;
            }
            case PlayerHasCompletedQuestlineQuestCount: // 242
            {
                var questLineQuests = global.getDB2Mgr().GetQuestsForQuestLine(reqValue);

                if (questLineQuests.isEmpty()) {
                    return false;
                }

                int completedQuests = 0;

                for (var questLineQuest : questLineQuests) {
                    if (referencePlayer.getQuestRewardStatus(questLineQuest.questID)) {
                        ++completedQuests;
                    }
                }

                if (completedQuests < reqValue) {
                    return false;
                }

                break;
            }
            case PlayerHasCompletedPercentageOfQuestline: // 243
            {
                var questLineQuests = global.getDB2Mgr().GetQuestsForQuestLine(reqValue);

                if (questLineQuests.isEmpty()) {
                    return false;
                }

                var completedQuests = 0;

                for (var questLineQuest : questLineQuests) {
                    if (referencePlayer.getQuestRewardStatus(questLineQuest.questID)) {
                        ++completedQuests;
                    }
                }

                if (MathUtil.GetPctOf(completedQuests, questLineQuests.size()) < reqValue) {
                    return false;
                }

                break;
            }
            case PlayerHasWarModeEnabled: // 244
                if (!referencePlayer.hasPlayerLocalFlag(PlayerLocalFlags.WarMode)) {
                    return false;
                }

                break;
            case PlayerIsOnWarModeShard: // 245
                if (!referencePlayer.hasPlayerFlag(playerFlags.WarModeActive)) {
                    return false;
                }

                break;
            case PlayerIsAllowedToToggleWarModeInArea: // 246
                if (!referencePlayer.canEnableWarModeInArea()) {
                    return false;
                }

                break;
            case MythicPlusKeystoneLevelEqualOrGreaterThan: // 247 NYI
            case MythicPlusCompletedInTime: // 248 NYI
            case MythicPlusMapChallengeMode: // 249 NYI
            case MythicPlusDisplaySeason: // 250 NYI
            case MythicPlusMilestoneSeason: // 251 NYI
                return false;
            case PlayerVisibleRace: // 252
            {
                var creatureDisplayInfo = CliDB.CreatureDisplayInfoStorage.get(referencePlayer.getDisplayId());

                if (creatureDisplayInfo == null) {
                    return false;
                }

                var creatureDisplayInfoExtra = CliDB.CreatureDisplayInfoExtraStorage.get(creatureDisplayInfo.ExtendedDisplayInfoID);

                if (creatureDisplayInfoExtra == null) {
                    return false;
                }

                if (creatureDisplayInfoExtra.DisplayRaceID != reqValue) {
                    return false;
                }

                break;
            }
            case TargetVisibleRace: // 253
            {
                if (refe == null || !refe.isUnit()) {
                    return false;
                }

                var creatureDisplayInfo = CliDB.CreatureDisplayInfoStorage.get(refe.toUnit().getDisplayId());

                if (creatureDisplayInfo == null) {
                    return false;
                }

                var creatureDisplayInfoExtra = CliDB.CreatureDisplayInfoExtraStorage.get(creatureDisplayInfo.ExtendedDisplayInfoID);

                if (creatureDisplayInfoExtra == null) {
                    return false;
                }

                if (creatureDisplayInfoExtra.DisplayRaceID != reqValue) {
                    return false;
                }

                break;
            }
            case FriendshipRepReactionEqual: // 254
            {
                var friendshipRepReaction = CliDB.FriendshipRepReactionStorage.get(reqValue);

                if (friendshipRepReaction == null) {
                    return false;
                }

                var friendshipReputation = CliDB.FriendshipReputationStorage.get(friendshipRepReaction.FriendshipRepID);

                if (friendshipReputation == null) {
                    return false;
                }

                var friendshipReactions = global.getDB2Mgr().GetFriendshipRepReactions(reqValue);

                if (friendshipReactions == null) {
                    return false;
                }

                var rank = referencePlayer.getReputationRank((int) friendshipReputation.factionID).getValue();

                if (rank >= friendshipReactions.size()) {
                    return false;
                }

                if (friendshipReactions.get(rank).id != reqValue) {
                    return false;
                }

                break;
            }
            case PlayerAuraStackCountEqual: // 255
                if (referencePlayer.getAuraCount((int) secondaryAsset) != reqValue) {
                    return false;
                }

                break;
            case TargetAuraStackCountEqual: // 256
                if (!refe || !refe.isUnit() || refe.toUnit().getAuraCount((int) secondaryAsset) != reqValue) {
                    return false;
                }

                break;
            case PlayerAuraStackCountEqualOrGreaterThan: // 257
                if (referencePlayer.getAuraCount((int) secondaryAsset) < reqValue) {
                    return false;
                }

                break;
            case TargetAuraStackCountEqualOrGreaterThan: // 258
                if (!refe || !refe.isUnit() || refe.toUnit().getAuraCount((int) secondaryAsset) < reqValue) {
                    return false;
                }

                break;
            case PlayerHasAzeriteEssenceRankLessThan: // 259
            {
                var heartOfAzeroth = referencePlayer.getItemByEntry(PlayerConst.ItemIdHeartOfAzeroth, ItemSearchLocation.Everywhere);

                if (heartOfAzeroth != null) {
                    var azeriteItem = heartOfAzeroth.getAsAzeriteItem();

                    if (azeriteItem != null) {
                        for (var essence : azeriteItem.AzeriteItemData.UnlockedEssences) {
                            if (essence.azeriteEssenceID == reqValue && essence.rank < secondaryAsset) {
                                return true;
                            }
                        }
                    }
                }

                return false;
            }
            case PlayerHasAzeriteEssenceRankEqual: // 260
            {
                var heartOfAzeroth = referencePlayer.getItemByEntry(PlayerConst.ItemIdHeartOfAzeroth, ItemSearchLocation.Everywhere);

                if (heartOfAzeroth != null) {
                    var azeriteItem = heartOfAzeroth.getAsAzeriteItem();

                    if (azeriteItem != null) {
                        for (var essence : azeriteItem.AzeriteItemData.UnlockedEssences) {
                            if (essence.azeriteEssenceID == reqValue && essence.rank == secondaryAsset) {
                                return true;
                            }
                        }
                    }
                }

                return false;
            }
            case PlayerHasAzeriteEssenceRankGreaterThan: // 261
            {
                var heartOfAzeroth = referencePlayer.getItemByEntry(PlayerConst.ItemIdHeartOfAzeroth, ItemSearchLocation.Everywhere);

                if (heartOfAzeroth != null) {
                    var azeriteItem = heartOfAzeroth.getAsAzeriteItem();

                    if (azeriteItem != null) {
                        for (var essence : azeriteItem.AzeriteItemData.UnlockedEssences) {
                            if (essence.azeriteEssenceID == reqValue && essence.rank > secondaryAsset) {
                                return true;
                            }
                        }
                    }
                }

                return false;
            }
            case PlayerHasAuraWithEffectIndex: // 262
                if (referencePlayer.getAuraEffect(reqValue, secondaryAsset) == null) {
                    return false;
                }

                break;
            case PlayerLootSpecializationMatchesRole: // 263
            {
                var spec = CliDB.ChrSpecializationStorage.get(referencePlayer.getPrimarySpecialization());

                if (spec == null || spec.role != reqValue) {
                    return false;
                }

                break;
            }
            case PlayerIsAtMaxExpansionLevel: // 264
                if (!referencePlayer.isMaxLevel()) {
                    return false;
                }

                break;
            case TransmogSource: // 265
            {
                var itemModifiedAppearance = CliDB.ItemModifiedAppearanceStorage.get(miscValue2);

                if (itemModifiedAppearance == null) {
                    return false;
                }

                if (itemModifiedAppearance.TransmogSourceTypeEnum != reqValue) {
                    return false;
                }

                break;
            }
            case PlayerHasAzeriteEssenceInSlotAtRankLessThan: // 266
            {
                var heartOfAzeroth = referencePlayer.getItemByEntry(PlayerConst.ItemIdHeartOfAzeroth, ItemSearchLocation.Everywhere);

                if (heartOfAzeroth != null) {
                    var azeriteItem = heartOfAzeroth.getAsAzeriteItem();

                    if (azeriteItem != null) {
                        var selectedEssences = azeriteItem.GetSelectedAzeriteEssences();

                        if (selectedEssences != null) {
                            for (var essence : azeriteItem.AzeriteItemData.UnlockedEssences) {
                                if (essence.azeriteEssenceID == selectedEssences.azeriteEssenceID.get((int) reqValue) && essence.rank < secondaryAsset) {
                                    return true;
                                }
                            }
                        }
                    }
                }

                return false;
            }
            case PlayerHasAzeriteEssenceInSlotAtRankGreaterThan: // 267
            {
                var heartOfAzeroth = referencePlayer.getItemByEntry(PlayerConst.ItemIdHeartOfAzeroth, ItemSearchLocation.Everywhere);

                if (heartOfAzeroth != null) {
                    var azeriteItem = heartOfAzeroth.getAsAzeriteItem();

                    if (azeriteItem != null) {
                        var selectedEssences = azeriteItem.GetSelectedAzeriteEssences();

                        if (selectedEssences != null) {
                            for (var essence : azeriteItem.AzeriteItemData.UnlockedEssences) {
                                if (essence.azeriteEssenceID == selectedEssences.azeriteEssenceID.get((int) reqValue) && essence.rank > secondaryAsset) {
                                    return true;
                                }
                            }
                        }
                    }
                }

                return false;
            }
            case PlayerLevelWithinContentTuning: // 268
            {
                var level = referencePlayer.getLevel();
                var levels = global.getDB2Mgr().GetContentTuningData(reqValue, 0);

                if (levels != null) {
                    if (secondaryAsset != 0) {
                        return level >= levels.getValue().MinLevelWithDelta && level <= levels.getValue().MaxLevelWithDelta;
                    }

                    return level >= levels.getValue().minLevel && level <= levels.getValue().maxLevel;
                }

                return false;
            }
            case TargetLevelWithinContentTuning: // 269
            {
                if (!refe || !refe.isUnit()) {
                    return false;
                }

                var level = refe.toUnit().getLevel();
                var levels = global.getDB2Mgr().GetContentTuningData(reqValue, 0);

                if (levels != null) {
                    if (secondaryAsset != 0) {
                        return level >= levels.getValue().MinLevelWithDelta && level <= levels.getValue().MaxLevelWithDelta;
                    }

                    return level >= levels.getValue().minLevel && level <= levels.getValue().maxLevel;
                }

                return false;
            }
            case PlayerIsScenarioInitiator: // 270 NYI
                return false;
            case PlayerHasCompletedQuestOrIsOnQuest: // 271
            {
                var status = referencePlayer.getQuestStatus(reqValue);

                if (status == QuestStatus.NONE || status == QuestStatus.Failed) {
                    return false;
                }

                break;
            }
            case PlayerLevelWithinOrAboveContentTuning: // 272
            {
                var level = referencePlayer.getLevel();
                var levels = global.getDB2Mgr().GetContentTuningData(reqValue, 0);

                if (levels != null) {
                    return secondaryAsset != 0 ? level >= levels.getValue().MinLevelWithDelta : level >= levels.getValue().minLevel;
                }

                return false;
            }
            case TargetLevelWithinOrAboveContentTuning: // 273
            {
                if (!refe || !refe.isUnit()) {
                    return false;
                }

                var level = refe.toUnit().getLevel();
                var levels = global.getDB2Mgr().GetContentTuningData(reqValue, 0);

                if (levels != null) {
                    return secondaryAsset != 0 ? level >= levels.getValue().MinLevelWithDelta : level >= levels.getValue().minLevel;
                }

                return false;
            }
            case PlayerLevelWithinOrAboveLevelRange: // 274 NYI
            case TargetLevelWithinOrAboveLevelRange: // 275 NYI
                return false;
            case MaxJailersTowerLevelEqualOrGreaterThan: // 276
                if (referencePlayer.getActivePlayerData().jailersTowerLevelMax < reqValue) {
                    return false;
                }

                break;
            case GroupedWithRaFRecruit: // 277
            {
                var group = referencePlayer.getGroup();

                if (group == null) {
                    return false;
                }

                for (var itr = group.getFirstMember(); itr != null; itr = itr.next()) {
                    if (itr.getSource().getSession().getRecruiterId() == referencePlayer.getSession().getAccountId()) {
                        return true;
                    }
                }

                return false;
            }
            case GroupedWithRaFRecruiter: // 278
            {
                var group = referencePlayer.getGroup();

                if (group == null) {
                    return false;
                }

                for (var itr = group.getFirstMember(); itr != null; itr = itr.next()) {
                    if (itr.getSource().getSession().getAccountId() == referencePlayer.getSession().getRecruiterId()) {
                        return true;
                    }
                }

                return false;
            }
            case PlayerSpecialization: // 279
                if (referencePlayer.getPrimarySpecialization() != reqValue) {
                    return false;
                }

                break;
            case PlayerMapOrCosmeticChildMap: // 280
            {
                var map = referencePlayer.getMap().getEntry();

                if (map.id != reqValue && map.CosmeticParentMapID != reqValue) {
                    return false;
                }

                break;
            }
            case PlayerCanAccessShadowlandsPrepurchaseContent: // 281
                if (referencePlayer.getSession().getAccountExpansion().getValue() < expansion.ShadowLands.getValue()) {
                    return false;
                }

                break;
            case PlayerHasEntitlement: // 282 NYI
            case PlayerIsInPartySyncGroup: // 283 NYI
            case QuestHasPartySyncRewards: // 284 NYI
            case HonorGainSource: // 285 NYI
            case JailersTowerActiveFloorIndexEqualOrGreaterThan: // 286 NYI
            case JailersTowerActiveFloorDifficultyEqualOrGreaterThan: // 287 NYI
                return false;
            case PlayerCovenant: // 288
                if (referencePlayer.getPlayerData().covenantID != reqValue) {
                    return false;
                }

                break;
            case HasTimeEventPassed: // 289
            {
                var eventTimestamp = GameTime.getGameTime();

                switch (reqValue) {
                    case 111: // Battle for Azeroth Season 4 Start
                        eventTimestamp = 1579618800L; // January 21, 2020 8:00

                        break;
                    case 120: // Patch 9.0.1
                        eventTimestamp = 1602601200L; // October 13, 2020 8:00

                        break;
                    case 121: // Shadowlands Season 1 Start
                        eventTimestamp = 1607439600L; // December 8, 2020 8:00

                        break;
                    case 123: // Shadowlands Season 1 End
                        // timestamp = unknown
                        break;

                    ;
                    case 149: // Shadowlands Season 2 End
                        // timestamp = unknown
                        break;
                    default:
                        break;
                }

                if (GameTime.getGameTime() < eventTimestamp) {
                    return false;
                }

                break;
            }
            case GarrisonHasPermanentTalent: // 290 NYI
                return false;
            case HasActiveSoulbind: // 291
                if (referencePlayer.getPlayerData().soulbindID != reqValue) {
                    return false;
                }

                break;
            case HasMemorizedSpell: // 292 NYI
                return false;
            case PlayerHasAPACSubscriptionReward_2020: // 293
            case PlayerHasTBCCDEWarpStalker_Mount: // 294
            case PlayerHasTBCCDEDarkPortal_Toy: // 295
            case PlayerHasTBCCDEPathOfIllidan_Toy: // 296
            case PlayerHasImpInABallToySubscriptionReward: // 297
                return false;
            case PlayerIsInAreaGroup: // 298
            {
                var areas = global.getDB2Mgr().GetAreasForGroup(reqValue);
                var area = CliDB.AreaTableStorage.get(referencePlayer.getAreaId());

                if (area != null) {
                    for (var areaInGroup : areas) {
                        if (areaInGroup == area.id || areaInGroup == area.ParentAreaID) {
                            return true;
                        }
                    }
                }

                return false;
            }
            case TargetIsInAreaGroup: // 299
            {
                if (!refe) {
                    return false;
                }

                var areas = global.getDB2Mgr().GetAreasForGroup(reqValue);
                var area = CliDB.AreaTableStorage.get(refe.getAreaId());

                if (area != null) {
                    for (var areaInGroup : areas) {
                        if (areaInGroup == area.id || areaInGroup == area.ParentAreaID) {
                            return true;
                        }
                    }
                }

                return false;
            }
            case PlayerIsInChromieTime: // 300
                if (referencePlayer.getActivePlayerData().uiChromieTimeExpansionID != reqValue) {
                    return false;
                }

                break;
            case PlayerIsInAnyChromieTime: // 301
                if (referencePlayer.getActivePlayerData().uiChromieTimeExpansionID == 0) {
                    return false;
                }

                break;
            case ItemIsAzeriteArmor: // 302
                if (global.getDB2Mgr().GetAzeriteEmpoweredItem((int) miscValue1) == null) {
                    return false;
                }

                break;
            case PlayerHasRuneforgePower: // 303
            {
                var block = (int) reqValue / 32;

                if (block >= referencePlayer.getActivePlayerData().runeforgePowers.size()) {
                    return false;
                }

                var bit = reqValue % 32;

                return (referencePlayer.getActivePlayerData().runeforgePowers.get(block) & (1 << (int) bit)) != 0;
            }
            case PlayerInChromieTimeForScaling: // 304
                if ((referencePlayer.getPlayerData().ctrOptions.getValue().contentTuningConditionMask & 1) == 0) {
                    return false;
                }

                break;
            case IsRaFRecruit: // 305
                if (referencePlayer.getSession().getRecruiterId() == 0) {
                    return false;
                }

                break;
            case AllPlayersInGroupHaveAchievement: // 306
            {
                var group = referencePlayer.getGroup();

                if (group != null) {
                    for (var itr = group.getFirstMember(); itr != null; itr = itr.next()) {
                        if (!itr.getSource().hasAchieved(reqValue)) {
                            return false;
                        }
                    }
                } else if (!referencePlayer.hasAchieved(reqValue)) {
                    return false;
                }

                break;
            }
            case PlayerHasSoulbindConduitRankEqualOrGreaterThan: // 307 NYI
                return false;
            case PlayerSpellShapeshiftFormCreatureDisplayInfoSelection: // 308
            {
                var formModelData = global.getDB2Mgr().GetShapeshiftFormModelData(referencePlayer.getRace(), referencePlayer.getNativeGender(), ShapeShiftForm.forValue(secondaryAsset));

                if (formModelData == null) {
                    return false;
                }

                var formChoice = referencePlayer.getCustomizationChoice(formModelData.OptionID);
                var choiceIndex = tangible.ListHelper.findIndex(formModelData.Choices, choice ->
                {
                    return choice.id == formChoice;
                });

                if (choiceIndex == -1) {
                    return false;
                }

                if (reqValue != formModelData.Displays.get(choiceIndex).displayID) {
                    return false;
                }

                break;
            }
            case PlayerSoulbindConduitCountAtRankEqualOrGreaterThan: // 309 NYI
                return false;
            case PlayerIsRestrictedAccount: // 310
                return false;
            case PlayerIsFlying: // 311
                if (!referencePlayer.isFlying()) {
                    return false;
                }

                break;
            case PlayerScenarioIsLastStep: // 312
            {
                var scenario = referencePlayer.getScenario();

                if (scenario == null) {
                    return false;
                }

                if (scenario.getStep() != scenario.getLastStep()) {
                    return false;
                }

                break;
            }
            case PlayerHasWeeklyRewardsAvailable: // 313
                if (referencePlayer.getActivePlayerData().weeklyRewardsPeriodSinceOrigin == 0) {
                    return false;
                }

                break;
            case TargetCovenant: // 314
                if (!refe || !refe.isPlayer()) {
                    return false;
                }

                if (refe.toPlayer().getPlayerData().covenantID != reqValue) {
                    return false;
                }

                break;
            case PlayerHasTBCCollectorsEdition: // 315
            case PlayerHasWrathCollectorsEdition: // 316
                return false;
            case GarrisonTalentResearchedAndAtRankEqualOrGreaterThan: // 317 NYI
            case CurrencySpentOnGarrisonTalentResearchEqualOrGreaterThan: // 318 NYI
            case RenownCatchupActive: // 319 NYI
            case RapidRenownCatchupActive: // 320 NYI
            case PlayerMythicPlusRatingEqualOrGreaterThan: // 321 NYI
            case PlayerMythicPlusRunCountInCurrentExpansionEqualOrGreaterThan: // 322 NYI
                return false;
            case PlayerHasCustomizationChoice: // 323
            {
                var customizationChoiceIndex = referencePlayer.getPlayerData().customizations.FindIndexIf(choice ->
                {
                    return choice.chrCustomizationChoiceID == reqValue;
                });

                if (customizationChoiceIndex < 0) {
                    return false;
                }

                break;
            }
            case PlayerBestWeeklyWinPvpTier: // 324
            {
                var pvpTier = CliDB.PvpTierStorage.get(reqValue);

                if (pvpTier == null) {
                    return false;
                }

                var pvpInfo = referencePlayer.getPvpInfoForBracket((byte) pvpTier.BracketID);

                if (pvpInfo == null) {
                    return false;
                }

                if (pvpTier.id != pvpInfo.weeklyBestWinPvpTierID) {
                    return false;
                }

                break;
            }
            case PlayerBestWeeklyWinPvpTierInBracketEqualOrGreaterThan: // 325
            {
                var pvpInfo = referencePlayer.getPvpInfoForBracket((byte) secondaryAsset);

                if (pvpInfo == null) {
                    return false;
                }

                var pvpTier = CliDB.PvpTierStorage.get(pvpInfo.weeklyBestWinPvpTierID);

                if (pvpTier == null) {
                    return false;
                }

                if (pvpTier.rank < reqValue) {
                    return false;
                }

                break;
            }
            case PlayerHasVanillaCollectorsEdition: // 326
                return false;
            case PlayerHasItemWithKeystoneLevelModifierEqualOrGreaterThan: // 327
            {
                var bagScanReachedEnd = referencePlayer.forEachItem(ItemSearchLocation.Inventory, item ->
                {
                    if (item.entry != reqValue) {
                        return true;
                    }

                    if (item.getModifier(ItemModifier.ChallengeKeystoneLevel) < secondaryAsset) {
                        return true;
                    }

                    return false;
                });

                if (bagScanReachedEnd) {
                    return false;
                }

                break;
            }
            case PlayerAuraWithLabelStackCountEqualOrGreaterThan: // 335
            {
                int count = 0;
                referencePlayer.getAuraQuery().hasLabel(new integer(secondaryAsset)).forEachResult(aura -> count += aura.stackAmount);

                if (count < reqValue) {
                    return false;
                }

                break;
            }
            case PlayerAuraWithLabelStackCountEqual: // 336
            {
                int count = 0;
                referencePlayer.getAuraQuery().hasLabel(new integer(secondaryAsset)).forEachResult(aura -> count += aura.stackAmount);

                if (count != reqValue) {
                    return false;
                }

                break;
            }
            case PlayerAuraWithLabelStackCountEqualOrLessThan: // 337
            {
                int count = 0;
                referencePlayer.getAuraQuery().hasLabel(new integer(secondaryAsset)).forEachResult(aura -> count += aura.stackAmount);

                if (count > reqValue) {
                    return false;
                }

                break;
            }
            case PlayerIsInCrossFactionGroup: // 338
            {
                var group = referencePlayer.getGroup();

                if (!group.getGroupFlags().hasFlag(GroupFlags.CrossFaction)) {
                    return false;
                }

                break;
            }
            case PlayerHasTraitNodeEntryInActiveConfig: // 340
            {
                boolean hasTraitNodeEntry ()
                {
                    for (var traitConfig : referencePlayer.getActivePlayerData().traitConfigs) {
                        if (TraitConfigType.forValue((int) traitConfig.type) == TraitConfigType.Combat) {
                            if (referencePlayer.getActivePlayerData().activeCombatTraitConfigID != traitConfig.ID || !(TraitCombatConfigFlags.forValue((int) traitConfig.combatConfigFlags)).hasFlag(TraitCombatConfigFlags.ActiveForSpec)) {
                                continue;
                            }
                        }

                        for (var traitEntry : traitConfig.entries) {
                            if (traitEntry.traitNodeEntryID == reqValue) {
                                return true;
                            }
                        }
                    }

                    return false;
                }

                if (!hasTraitNodeEntry()) {
                    return false;
                }

                break;
            }
            case PlayerHasTraitNodeEntryInActiveConfigRankGreaterOrEqualThan: // 341
            {
                var traitNodeEntryRank = () ->
                {
                    for (var traitConfig : referencePlayer.getActivePlayerData().traitConfigs) {
                        if (TraitConfigType.forValue((int) traitConfig.type) == TraitConfigType.Combat) {
                            if (referencePlayer.getActivePlayerData().activeCombatTraitConfigID != traitConfig.ID || !(TraitCombatConfigFlags.forValue((int) traitConfig.combatConfigFlags)).hasFlag(TraitCombatConfigFlags.ActiveForSpec)) {
                                continue;
                            }
                        }

                        for (var traitEntry : traitConfig.entries) {
                            if (traitEntry.traitNodeEntryID == secondaryAsset) {
                                return (short) traitEntry.rank;
                            }
                        }
                    }

                    return null;
                } ();

                if (!traitNodeEntryRank.HasValue || traitNodeEntryRank < reqValue) {
                    return false;
                }

                break;
            }
            case PlayerDaysSinceLogout: // 344
                if (GameTime.getGameTime() - referencePlayer.getPlayerData().logoutTime < reqValue * time.Day) {
                    return false;
                }

                break;
            case PlayerHasPerksProgramPendingReward: // 350
                if (!referencePlayer.getActivePlayerData().hasPerksProgramPendingReward) {
                    return false;
                }

                break;
            case PlayerCanUseItem: // 351
            {
                var itemTemplate = global.getObjectMgr().getItemTemplate(reqValue);

                if (itemTemplate == null || referencePlayer.canUseItem(itemTemplate) != InventoryResult.Ok) {
                    return false;
                }

                break;
            }
            case PlayerHasAtLeastProfPathRanks: // 355
            {
                int ranks = 0;

                for (var traitConfig : referencePlayer.getActivePlayerData().traitConfigs) {
                    if (TraitConfigType.forValue((int) traitConfig.type) != TraitConfigType.profession) {
                        continue;
                    }

                    if (traitConfig.skillLineID != secondaryAsset) {
                        continue;
                    }

                    for (var traitEntry : traitConfig.entries) {
                        if ((CliDB.TraitNodeEntryStorage.get(traitEntry.traitNodeEntryID) == null ? null : CliDB.TraitNodeEntryStorage.get(traitEntry.traitNodeEntryID).GetNodeEntryType()) == TraitNodeEntryType.ProfPath) {
                            ranks += (int) (traitEntry.rank + traitEntry.grantedRanks);
                        }
                    }
                }

                if (ranks < reqValue) {
                    return false;
                }

                break;
            }
            default:
                return false;
        }

        return true;
    }
}
