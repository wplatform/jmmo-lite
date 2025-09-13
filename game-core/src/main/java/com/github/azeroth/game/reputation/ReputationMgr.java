package com.github.azeroth.game.reputation;


import com.badlogic.gdx.utils.IntMap;
import com.github.azeroth.dbc.domain.Faction;
import com.github.azeroth.dbc.domain.FactionTemplate;
import com.github.azeroth.defines.ReputationRank;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.packet.reputation.ForcedReaction;
import com.github.azeroth.game.networking.packet.reputation.SetForcedReactions;
import com.github.azeroth.game.listener.interfaces.iplayer.IPlayerOnReputationChange;

import java.util.TreeMap;

public class ReputationMgr {
    public static final int[] REPUTATIONRANKTHRESHOLDS = {-42000, -6000, -3000, 0, 3000, 9000, 21000, 42000};

    public static final SysMessage[] REPUTATIONRANKSTRINDEX = {SysMessage.RepHated, SysMessage.RepHostile, SysMessage.RepUnfriendly, SysMessage.RepNeutral, SysMessage.RepFriendly, SysMessage.RepHonored, SysMessage.RepRevered, SysMessage.RepExalted};


    private final Player player;
    private final TreeMap<Integer, FactionState> factions = new TreeMap<Integer, FactionState>();
    private final IntMap<ReputationRank> forcedReactions = new IntMap<>();
    private byte visibleFactionCount;
    private byte honoredFactionCount;
    private byte reveredFactionCount;
    private byte exaltedFactionCount;
    private boolean sendFactionIncreased; //! Play visual effect on next SMSG_SET_FACTION_STANDING sent

    public ReputationMgr(Player owner) {
        player = owner;
        visibleFactionCount = 0;
        honoredFactionCount = 0;
        reveredFactionCount = 0;
        exaltedFactionCount = 0;
        sendFactionIncreased = false;
    }

    // this allows calculating base reputations to offline players, just by race and class
    public static int getBaseReputationOf(Faction factionEntry, Race race, PlayerClass playerClass) {
        if (factionEntry == null) {
            return 0;
        }

        var raceMask = SharedConst.GetMaskForRace(race);
        var classMask = (1 << (playerClass.getValue() - 1));

        for (var i = 0; i < 4; i++) {
            if ((factionEntry.ReputationClassMask[i] == 0 || factionEntry.ReputationClassMask[i].hasFlag((short) classMask)) && (factionEntry.ReputationRaceMask[i] == 0 || factionEntry.ReputationRaceMask[i].hasFlag(raceMask))) {
                return factionEntry.ReputationBase[i];
            }
        }

        return 0;
    }

    public final byte getVisibleFactionCount() {
        return visibleFactionCount;
    }

    public final byte getHonoredFactionCount() {
        return honoredFactionCount;
    }

    public final byte getReveredFactionCount() {
        return reveredFactionCount;
    }

    public final byte getExaltedFactionCount() {
        return exaltedFactionCount;
    }

    public final TreeMap<Integer, FactionState> getStateList() {
        return factions;
    }

    public final FactionState getState(Faction factionEntry) {
        return factionEntry.CanHaveReputation() ? getState(factionEntry.ReputationIndex) : null;
    }

    public final boolean isAtWar(int factionId) {
        var factionEntry = CliDB.FactionStorage.get(factionId);

        if (factionEntry == null) {
            return false;
        }

        return isAtWar(factionEntry);
    }

    public final boolean isAtWar(Faction factionEntry) {
        if (factionEntry == null) {
            return false;
        }

        var factionState = getState(factionEntry);

        if (factionState != null) {
            return factionState.flags.hasFlag(ReputationFlags.AtWar);
        }

        return false;
    }

    public final int getReputation(int faction_id) {
        var factionEntry = CliDB.FactionStorage.get(faction_id);

        if (factionEntry == null) {
            Log.outError(LogFilter.player, "ReputationMgr.GetReputation: Can't get reputation of {0} for unknown faction (faction id) #{1}.", player.getName(), faction_id);

            return 0;
        }

        return getReputation(factionEntry);
    }

    public final int getBaseReputation(Faction factionEntry) {
        var dataIndex = getFactionDataIndexForRaceAndClass(factionEntry);

        if (dataIndex < 0) {
            return 0;
        }

        return factionEntry.ReputationBase[dataIndex];
    }

    public final int getReputation(Faction factionEntry) {
        // Faction without recorded reputation. Just ignore.
        if (factionEntry == null) {
            return 0;
        }

        var state = getState(factionEntry);

        if (state != null) {
            return getBaseReputation(factionEntry) + state.standing;
        }

        return 0;
    }

    public final ReputationRank getRank(Faction factionEntry) {
        var reputation = getReputation(factionEntry);

        return reputationToRank(factionEntry, reputation);
    }

    public final ReputationRank getForcedRankIfAny(FactionTemplate factionTemplateEntry) {
        return getForcedRankIfAny(factionTemplateEntry.getFaction());
    }

    public final int getParagonLevel(int paragonFactionId) {

        return getParagonLevel(CliDB.FactionStorage.get(paragonFactionId));
    }

    public final void applyForceReaction(int faction_id, ReputationRank rank, boolean apply) {
        if (apply) {
            forcedReactions.put(faction_id, rank);
        } else {
            forcedReactions.remove(faction_id);
        }
    }

    public final void sendForceReactions() {
        SetForcedReactions setForcedReactions = new SetForcedReactions();

        for (var pair : forcedReactions) {
            ForcedReaction forcedReaction = new ForcedReaction();
            forcedReaction.faction = pair.key;
            forcedReaction.reaction = pair.value.ordinal();

            setForcedReactions.reactions.add(forcedReaction);
        }

        player.sendPacket(setForcedReactions);
    }

    public final void sendState(FactionState faction) {
        SetFactionStanding setFactionStanding = new SetFactionStanding();
        setFactionStanding.bonusFromAchievementSystem = 0.0f;

        var standing = faction.visualStandingIncrease != 0 ? faction.VisualStandingIncrease : faction.standing;

        if (faction != null) {
            setFactionStanding.faction.add(new FactionStandingData((int) faction.reputationListID, standing));
        }

        for (var state : factions.values()) {
            if (state.needSend) {
                state.needSend = false;

                if (faction == null || state.reputationListID != faction.reputationListID) {
                    standing = state.visualStandingIncrease != 0 ? state.VisualStandingIncrease : state.standing;
                    setFactionStanding.faction.add(new FactionStandingData((int) state.reputationListID, standing));
                }
            }
        }

        setFactionStanding.showVisual = sendFactionIncreased;
        player.sendPacket(setFactionStanding);

        sendFactionIncreased = false; // Reset
    }

    public final void sendInitialReputations() {
        InitializeFactions initFactions = new InitializeFactions();

        for (var pair : factions.entrySet()) {
            initFactions.FactionFlags[pair.getKey()] = pair.getValue().flags;
            initFactions.FactionStandings[pair.getKey()] = pair.getValue().standing;
            // @todo faction bonus
            pair.getValue().needSend = false;
        }

        player.sendPacket(initFactions);
    }

    public final void sendVisible(FactionState faction) {
        sendVisible(faction, true);
    }

    public final void sendVisible(FactionState faction, boolean visible) {
        if (player.getSession().getPlayerLoading()) {
            return;
        }

        //make faction visible / not visible in reputation list at client
        SetFactionVisible packet = new SetFactionVisible(visible);
        packet.factionIndex = faction.reputationListID;
        player.sendPacket(packet);
    }

    public final boolean modifyReputation(Faction factionEntry, int standing, boolean spillOverOnly) {
        return modifyReputation(factionEntry, standing, spillOverOnly, false);
    }

    public final boolean modifyReputation(Faction factionEntry, int standing) {
        return modifyReputation(factionEntry, standing, false, false);
    }

    public final boolean modifyReputation(Faction factionEntry, int standing, boolean spillOverOnly, boolean noSpillover) {
        return setReputation(factionEntry, standing, true, spillOverOnly, noSpillover);
    }

    public final boolean setReputation(Faction factionEntry, double standing) {
        return setReputation(factionEntry, (int) standing);
    }

    public final boolean setReputation(Faction factionEntry, int standing) {
        return setReputation(factionEntry, standing, false, false, false);
    }

    public final boolean setReputation(Faction factionEntry, int standing, boolean incremental, boolean spillOverOnly, boolean noSpillover) {
        global.getScriptMgr().<IPlayerOnReputationChange>ForEach(p -> p.OnReputationChange(player, factionEntry.id, standing, incremental));
        var res = false;

        if (!noSpillover) {
            // if spillover definition exists in DB, override DBC
            var repTemplate = global.getObjectMgr().getRepSpillover(factionEntry.id);

            if (repTemplate != null) {
                for (int i = 0; i < 5; ++i) {
                    if (repTemplate.Faction[i] != 0) {
                        if (player.getReputationRank(repTemplate.Faction[i]) <= ReputationRank.forValue(repTemplate.FactionRank[i])) {
                            // bonuses are already given, so just modify standing by rate
                            var spilloverRep = (int) (standing * repTemplate.FactionRate[i]);
                            setOneFactionReputation(CliDB.FactionStorage.get(repTemplate.Faction[i]), spilloverRep, incremental);
                        }
                    }
                }
            } else {
                float spillOverRepOut = standing;
                // check for sub-factions that receive spillover
                var flist = global.getDB2Mgr().GetFactionTeamList(factionEntry.id);

                // if has no sub-factions, check for factions with same parent
                if (flist == null && factionEntry.ParentFactionID != 0 && factionEntry.ParentFactionMod[1] != 0.0f) {
                    spillOverRepOut *= factionEntry.ParentFactionMod[1];
                    var parent = CliDB.FactionStorage.get(factionEntry.ParentFactionID);

                    if (parent != null) {
                        var parentState = factions.get(parent.ReputationIndex);

                        // some team factions have own reputation standing, in this case do not spill to other sub-factions
                        if (parentState != null && parentState.flags.hasFlag(ReputationFlags.HeaderShowsBar)) {
                            setOneFactionReputation(parent, (int) spillOverRepOut, incremental);
                        } else // spill to "sister" factions
                        {
                            flist = global.getDB2Mgr().GetFactionTeamList(factionEntry.ParentFactionID);
                        }
                    }
                }

                if (flist != null) {
                    // Spillover to affiliated factions
                    for (var id : flist) {
                        var factionEntryCalc = CliDB.FactionStorage.get(id);

                        if (factionEntryCalc != null) {
                            if (factionEntryCalc == factionEntry || getRank(factionEntryCalc) > ReputationRank.forValue(factionEntryCalc.ParentFactionMod[0])) {
                                continue;
                            }

                            var spilloverRep = (int) (spillOverRepOut * factionEntryCalc.ParentFactionMod[0]);

                            if (spilloverRep != 0 || !incremental) {
                                res = setOneFactionReputation(factionEntryCalc, spilloverRep, incremental);
                            }
                        }
                    }
                }
            }
        }

        // spillover done, update faction itself
        var faction = factions.get(factionEntry.ReputationIndex);

        if (faction != null) {
            var primaryFactionToModify = factionEntry;

            if (incremental && standing > 0 && canGainParagonReputationForFaction(factionEntry)) {
                primaryFactionToModify = CliDB.FactionStorage.get(factionEntry.ParagonFactionID);
                faction = factions.get(primaryFactionToModify.ReputationIndex);
            }

            if (faction != null) {
                // if we update spillover only, do not update main reputation (rank exceeds creature reward rate)
                if (!spillOverOnly) {
                    res = setOneFactionReputation(primaryFactionToModify, standing, incremental);
                }

                // only this faction gets reported to client, even if it has no own visible standing
                sendState(faction);
            }
        }

        return res;
    }

    public final boolean setOneFactionReputation(Faction factionEntry, int standing, boolean incremental) {
        var factionState = factions.get((int) factionEntry.ReputationIndex);

        if (factionState != null) {
            // Ignore renown reputation already raised to the maximum level
            if (hasMaximumRenownReputation(factionEntry) && standing > 0) {
                factionState.needSend = false;
                factionState.needSave = false;

                return false;
            }

            var baseRep = getBaseReputation(factionEntry);
            var oldStanding = factionState.standing + baseRep;

            if (incremental || isRenownReputation(factionEntry)) {
                // int32 *= float cause one point loss?
                standing = (int) (Math.floor(standing * WorldConfig.getFloatValue(WorldCfg.RateReputationGain) + 0.5f));
                standing += oldStanding;
            }

            if (standing > getMaxReputation(factionEntry)) {
                standing = getMaxReputation(factionEntry);
            } else if (standing < getMinReputation(factionEntry)) {
                standing = getMinReputation(factionEntry);
            }

            // Ignore rank for paragon or renown reputation
            if (!isParagonReputation(factionEntry) && !isRenownReputation(factionEntry)) {
                var oldRank = reputationToRank(factionEntry, oldStanding);
                var newRank = reputationToRank(factionEntry, standing);

                if (newRank.getValue() <= ReputationRank.Hostile.getValue()) {
                    setAtWar(factionState, true);
                }

                if (newRank.getValue() > oldRank.getValue()) {
                    sendFactionIncreased = true;
                }

                if (factionEntry.FriendshipRepID == 0) {
                    updateRankCounters(oldRank, newRank);
                }
            } else {
                sendFactionIncreased = true; // TODO: Check Paragon reputation
            }

            // Calculate new standing and reputation change
            var newStanding = 0;
            var reputationChange = standing - oldStanding;

            if (!isRenownReputation(factionEntry)) {
                newStanding = standing - baseRep;
            } else {
                var currency = CliDB.CurrencyTypesStorage.get(factionEntry.RenownCurrencyID);

                if (currency != null) {
                    var renownLevelThreshold = getRenownLevelThreshold(factionEntry);
                    var oldRenownLevel = getRenownLevel(factionEntry);

                    var totalReputation = (oldRenownLevel * renownLevelThreshold) + (standing - baseRep);
                    var newRenownLevel = totalReputation / renownLevelThreshold;
                    newStanding = totalReputation % renownLevelThreshold;

                    if (newRenownLevel >= getRenownMaxLevel(factionEntry)) {
                        newStanding = 0;
                        reputationChange += (getRenownMaxLevel(factionEntry) * renownLevelThreshold) - totalReputation;
                    }

                    factionState.visualStandingIncrease = reputationChange;

                    // If the reputation is decreased by command, we will send CurrencyDestroyReason::Cheat
                    if (oldRenownLevel != newRenownLevel) {
                        player.modifyCurrency(currency.id, newRenownLevel - oldRenownLevel, CurrencyGainSource.RenownRepGain, CurrencyDestroyReason.Cheat);
                    }
                }
            }

            player.reputationChanged(factionEntry, reputationChange);

            factionState.standing = newStanding;
            factionState.needSend = true;
            factionState.needSave = true;

            setVisible(factionState);

            var paragonReputation = global.getDB2Mgr().GetParagonReputation(factionEntry.id);

            if (paragonReputation != null) {
                var oldParagonLevel = oldStanding / paragonReputation.LevelThreshold;
                var newParagonLevel = standing / paragonReputation.LevelThreshold;

                if (oldParagonLevel != newParagonLevel) {
                    var paragonRewardQuest = global.getObjectMgr().getQuestTemplate((int) paragonReputation.questID);

                    if (paragonRewardQuest != null) {
                        player.addQuestAndCheckCompletion(paragonRewardQuest, null);
                    }
                }
            }

            player.updateCriteria(CriteriaType.TotalFactionsEncountered, factionEntry.id);
            player.updateCriteria(CriteriaType.ReputationGained, factionEntry.id);
            player.updateCriteria(CriteriaType.TotalExaltedFactions, factionEntry.id);
            player.updateCriteria(CriteriaType.TotalReveredFactions, factionEntry.id);
            player.updateCriteria(CriteriaType.TotalHonoredFactions, factionEntry.id);

            return true;
        }

        return false;
    }

    public final void setVisible(FactionTemplate factionTemplateEntry) {
        if (factionTemplateEntry.faction == 0) {
            return;
        }

        var factionEntry = CliDB.FactionStorage.get(factionTemplateEntry.faction);

        if (factionEntry.id != 0) {
            // Never show factions of the opposing team
            if (!(boolean) (factionEntry.ReputationRaceMask[1] & SharedConst.GetMaskForRace(player.getRace())) && factionEntry.ReputationBase[1] == SharedConst.ReputationBottom) {
                setVisible(factionEntry);
            }
        }
    }

    public final void setVisible(Faction factionEntry) {
        if (!factionEntry.CanHaveReputation()) {
            return;
        }

        var factionState = factions.get((int) factionEntry.ReputationIndex);

        if (factionState == null) {
            return;
        }

        setVisible(factionState);
    }

    public final void setAtWar(int repListID, boolean on) {
        var factionState = factions.get(repListID);

        if (factionState == null) {
            return;
        }

        // always invisible or hidden faction can't change war state
        if (factionState.flags.hasFlag(ReputationFlags.hidden.getValue() | ReputationFlags.header.getValue())) {
            return;
        }

        setAtWar(factionState, on);
    }

    public final void setInactive(int repListID, boolean on) {
        var factionState = factions.get(repListID);

        if (factionState == null) {
            return;
        }

        setInactive(factionState, on);
    }

    public final void loadFromDB(SQLResult result) {
        // Set initial reputations (so everything is nifty before DB data load)
        initialize();

        if (!result.isEmpty()) {
            do {
                var factionEntry = CliDB.FactionStorage.get(result.<Integer>Read(0));

                if (factionEntry != null && factionEntry.CanHaveReputation()) {
                    var faction = factions.get((int) factionEntry.ReputationIndex);

                    if (faction == null) {
                        continue;
                    }

                    // update standing to current
                    faction.standing = result.<Integer>Read(1);

                    // update counters
                    if (factionEntry.FriendshipRepID == 0) {
                        var BaseRep = getBaseReputation(factionEntry);
                        var old_rank = reputationToRank(factionEntry, BaseRep);
                        var new_rank = reputationToRank(factionEntry, BaseRep + faction.standing);
                        updateRankCounters(old_rank, new_rank);
                    }

                    var dbFactionFlags = ReputationFlags.forValue(result.<Integer>Read(2));

                    if (dbFactionFlags.hasFlag(ReputationFlags.Visible)) {
                        setVisible(faction); // have internal checks for forced invisibility
                    }

                    if (dbFactionFlags.hasFlag(ReputationFlags.inactive)) {
                        setInactive(faction, true); // have internal checks for visibility requirement
                    }

                    if (dbFactionFlags.hasFlag(ReputationFlags.AtWar)) // DB at war
                    {
                        setAtWar(faction, true); // have internal checks for FACTION_FLAG_PEACE_FORCED
                    } else // DB not at war
                    {
                        // allow remove if visible (and then not FACTION_FLAG_INVISIBLE_FORCED or FACTION_FLAG_HIDDEN)
                        if (faction.flags.hasFlag(ReputationFlags.Visible)) {
                            setAtWar(faction, false); // have internal checks for FACTION_FLAG_PEACE_FORCED
                        }
                    }

                    // set atWar for hostile
                    if (getRank(factionEntry) <= ReputationRank.Hostile.getValue()) {
                        setAtWar(faction, true);
                    }

                    // reset changed flag if values similar to saved in DB
                    if (faction.flags == dbFactionFlags) {
                        faction.needSend = false;
                        faction.needSave = false;
                    }
                }
            } while (result.NextRow());
        }
    }

    public final void saveToDB(SQLTransaction trans) {
        for (var factionState : factions.values()) {
            if (factionState.needSave) {
                var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_REPUTATION_BY_FACTION);
                stmt.AddValue(0, player.getGUID().getCounter());
                stmt.AddValue(1, factionState.id);
                trans.append(stmt);

                stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHAR_REPUTATION_BY_FACTION);
                stmt.AddValue(0, player.getGUID().getCounter());
                stmt.AddValue(1, factionState.id);
                stmt.AddValue(2, factionState.standing);
                stmt.AddValue(3, (short) factionState.flags);
                trans.append(stmt);

                factionState.needSave = false;
            }
        }
    }

    public final FactionState getState(int id) {
        return factions.get((int) id);
    }

    public final int getReputationRankStrIndex(Faction factionEntry) {
        return (int) ReputationRankStrIndex[GetRank(factionEntry).getValue()];
    }

    public final ReputationRank getForcedRankIfAny(int factionId) {
        return forcedReactions.get(factionId);
    }

    private <T> ReputationRank reputationToRankHelper(list<T> thresholds, int standing, tangible.Func1Param<T, Integer> thresholdExtractor) {
        var i = 0;
        var rank = -1;

        while (i != thresholds.size() - 1 && standing >= thresholdExtractor.invoke(thresholds.get(i))) {
            ++rank;
            ++i;
        }

        return ReputationRank.forValue(rank);
    }

    private ReputationRank reputationToRank(Faction factionEntry, int standing) {
        var rank = ReputationRank.min;

        var friendshipReactions = global.getDB2Mgr().GetFriendshipRepReactions(factionEntry.FriendshipRepID);

        if (!friendshipReactions.isEmpty()) {
            rank = reputationToRankHelper(friendshipReactions, standing, (FriendshipRepReaction frr) ->
            {
                return frr.ReactionThreshold;
            });
        } else {
            rank = reputationToRankHelper(REPUTATIONRANKTHRESHOLDS, standing, (int threshold) ->
            {
                return threshold;
            });
        }

        return rank;
    }

    private int getMinReputation(Faction factionEntry) {
        var friendshipReactions = global.getDB2Mgr().GetFriendshipRepReactions(factionEntry.FriendshipRepID);

        if (!friendshipReactions.isEmpty()) {
            return friendshipReactions.get(0).ReactionThreshold;
        }

        return ReputationRankThresholds[0];
    }

    private int getMaxReputation(Faction factionEntry) {
        var paragonReputation = global.getDB2Mgr().GetParagonReputation(factionEntry.id);

        if (paragonReputation != null) {
            // has reward quest, cap is just before threshold for another quest reward
            // for example: if current reputation is 12345 and quests are given every 10000 and player has unclaimed reward
            // then cap will be 19999

            // otherwise cap is one theshold level larger
            // if current reputation is 12345 and quests are given every 10000 and player does NOT have unclaimed reward
            // then cap will be 29999

            var reputation = getReputation(factionEntry);
            var cap = reputation + paragonReputation.LevelThreshold - reputation % paragonReputation.LevelThreshold - 1;

            if (player.getQuestStatus((int) paragonReputation.questID) == QuestStatus.NONE) {
                cap += paragonReputation.LevelThreshold;
            }

            return cap;
        }

        if (isRenownReputation(factionEntry)) {
            // Compared to a paragon reputation, DF renown reputations
            // have a maximum second of 2500 which resets with each level of renown acquired.
            // We calculate the total reputation necessary to raise the renown to the maximum
            return getRenownMaxLevel(factionEntry) * getRenownLevelThreshold(factionEntry);
        }

        var friendshipReactions = global.getDB2Mgr().GetFriendshipRepReactions(factionEntry.FriendshipRepID);

        if (!friendshipReactions.isEmpty()) {
            return friendshipReactions.LastOrDefault().ReactionThreshold;
        }

        var dataIndex = getFactionDataIndexForRaceAndClass(factionEntry);

        if (dataIndex >= 0) {
            return factionEntry.ReputationMax[dataIndex];
        }

        return REPUTATIONRANKTHRESHOLDS.LastOrDefault();
    }

    private ReputationRank getBaseRank(Faction factionEntry) {
        var reputation = getBaseReputation(factionEntry);

        return reputationToRank(factionEntry, reputation);
    }

    private boolean isParagonReputation(Faction factionEntry) {
        if (global.getDB2Mgr().GetParagonReputation(factionEntry.id) != null) {
            return true;
        }

        return false;
    }

    private int getParagonLevel(Faction paragonFactionEntry) {
        if (paragonFactionEntry == null) {
            return 0;
        }

        var paragonReputation = global.getDB2Mgr().GetParagonReputation(paragonFactionEntry.id);

        if (paragonReputation != null) {
            return getReputation(paragonFactionEntry) / paragonReputation.LevelThreshold;
        }

        return 0;
    }

    private boolean hasMaximumRenownReputation(Faction factionEntry) {
        if (!isRenownReputation(factionEntry)) {
            return false;
        }

        return getRenownLevel(factionEntry) >= getRenownMaxLevel(factionEntry);
    }

    private boolean isRenownReputation(Faction factionEntry) {
        return factionEntry.RenownCurrencyID > 0;
    }

    private int getRenownLevel(Faction renownFactionEntry) {
        if (renownFactionEntry == null) {
            return 0;
        }

        var currency = CliDB.CurrencyTypesStorage.get(renownFactionEntry.RenownCurrencyID);

        if (currency != null) {
            return (int) player.getCurrencyQuantity(currency.id);
        }

        return 0;
    }

    private int getRenownLevelThreshold(Faction renownFactionEntry) {
        if (renownFactionEntry == null || !isRenownReputation(renownFactionEntry)) {
            return 0;
        }

        var dataIndex = getFactionDataIndexForRaceAndClass(renownFactionEntry);

        if (dataIndex >= 0) {
            return renownFactionEntry.ReputationMax[dataIndex];
        }

        return 0;
    }

    private int getRenownMaxLevel(Faction renownFactionEntry) {
        if (renownFactionEntry == null) {
            return 0;
        }

        var currency = CliDB.CurrencyTypesStorage.get(renownFactionEntry.RenownCurrencyID);

        if (currency != null) {
            return (int) player.getCurrencyMaxQuantity(currency);
        }

        return 0;
    }

    private ReputationFlags getDefaultStateFlags(Faction factionEntry) {
        var flags = ReputationFlags.NONE;

        var dataIndex = getFactionDataIndexForRaceAndClass(factionEntry);

        if (dataIndex > 0) {
            flags = ReputationFlags.forValue(factionEntry.ReputationFlags[dataIndex]);
        }

        if (global.getDB2Mgr().GetParagonReputation(factionEntry.id) != null) {
            flags = ReputationFlags.forValue(flags.getValue() | ReputationFlags.ShowPropagated.getValue());
        }

        return flags;
    }

    private void initialize() {
        factions.clear();
        visibleFactionCount = 0;
        honoredFactionCount = 0;
        reveredFactionCount = 0;
        exaltedFactionCount = 0;
        sendFactionIncreased = false;

        for (var factionEntry : CliDB.FactionStorage.values()) {
            if (factionEntry.CanHaveReputation()) {
                FactionState newFaction = new FactionState();
                newFaction.id = factionEntry.id;
                newFaction.reputationListID = (int) factionEntry.ReputationIndex;
                newFaction.standing = 0;
                newFaction.visualStandingIncrease = 0;
                newFaction.flags = getDefaultStateFlags(factionEntry);
                newFaction.needSend = true;
                newFaction.needSave = true;

                if (newFaction.flags.hasFlag(ReputationFlags.Visible)) {
                    ++visibleFactionCount;
                }

                if (factionEntry.FriendshipRepID == 0) {
                    updateRankCounters(ReputationRank.Hostile, getBaseRank(factionEntry));
                }

                factions.put(newFaction.reputationListID, newFaction);
            }
        }
    }

    private void setVisible(FactionState faction) {
        // always invisible or hidden faction can't be make visible
        if (faction.flags.hasFlag(ReputationFlags.hidden)) {
            return;
        }

        if (faction.flags.hasFlag(ReputationFlags.header) && !faction.flags.hasFlag(ReputationFlags.HeaderShowsBar)) {
            return;
        }

        if (global.getDB2Mgr().GetParagonReputation(faction.id) != null) {
            return;
        }

        // already set
        if (faction.flags.hasFlag(ReputationFlags.Visible)) {
            return;
        }

        faction.flags = ReputationFlags.forValue(faction.flags.getValue() | ReputationFlags.Visible.getValue());
        faction.needSend = true;
        faction.needSave = true;

        visibleFactionCount++;

        sendVisible(faction);
    }

    private void setAtWar(FactionState faction, boolean atWar) {
        // Do not allow to declare war to our own faction. But allow for rival factions (eg Aldor vs Scryer).
        if (atWar && faction.flags.hasFlag(ReputationFlags.Peaceful) && getRank(CliDB.FactionStorage.get(faction.id)) > ReputationRank.Hated.getValue()) {
            return;
        }

        // already set
        if (faction.flags.hasFlag(ReputationFlags.AtWar) == atWar) {
            return;
        }

        if (atWar) {
            faction.flags = ReputationFlags.forValue(faction.flags.getValue() | ReputationFlags.AtWar.getValue());
        } else {
            faction.flags = ReputationFlags.forValue(faction.flags.getValue() & ~ReputationFlags.AtWar.getValue());
        }

        faction.needSend = true;
        faction.needSave = true;
    }

    private void setInactive(FactionState faction, boolean inactive) {
        // always invisible or hidden faction can't be inactive
        if (faction.flags.hasFlag(ReputationFlags.hidden.getValue() | ReputationFlags.header.getValue()) || !faction.flags.hasFlag(ReputationFlags.Visible)) {
            return;
        }

        // already set
        if (faction.flags.hasFlag(ReputationFlags.inactive) == inactive) {
            return;
        }

        if (inactive) {
            faction.flags = ReputationFlags.forValue(faction.flags.getValue() | ReputationFlags.inactive.getValue());
        } else {
            faction.flags = ReputationFlags.forValue(faction.flags.getValue() & ~ReputationFlags.inactive.getValue());
        }

        faction.needSend = true;
        faction.needSave = true;
    }

    private void updateRankCounters(ReputationRank old_rank, ReputationRank new_rank) {
        if (old_rank.getValue() >= ReputationRank.Exalted.getValue()) {
            --_exaltedFactionCount;
        }

        if (old_rank.getValue() >= ReputationRank.Revered.getValue()) {
            --_reveredFactionCount;
        }

        if (old_rank.getValue() >= ReputationRank.Honored.getValue()) {
            --_honoredFactionCount;
        }

        if (new_rank.getValue() >= ReputationRank.Exalted.getValue()) {
            ++exaltedFactionCount;
        }

        if (new_rank.getValue() >= ReputationRank.Revered.getValue()) {
            ++reveredFactionCount;
        }

        if (new_rank.getValue() >= ReputationRank.Honored.getValue()) {
            ++honoredFactionCount;
        }
    }

    private int getFactionDataIndexForRaceAndClass(Faction factionEntry) {
        if (factionEntry == null) {
            return -1;
        }

        var raceMask = SharedConst.GetMaskForRace(player.getRace());
        var classMask = (short) player.getClassMask();

        for (var i = 0; i < 4; i++) {
            if ((factionEntry.ReputationRaceMask[i].hasFlag(raceMask) || (factionEntry.ReputationRaceMask[i] == 0 && factionEntry.ReputationClassMask[i] != 0)) && (factionEntry.ReputationClassMask[i].hasFlag(classMask) || factionEntry.ReputationClassMask[i] == 0)) {

                return i;
            }
        }

        return -1;
    }

    private boolean canGainParagonReputationForFaction(Faction factionEntry) {
        if (!CliDB.FactionStorage.containsKey(factionEntry.ParagonFactionID)) {
            return false;
        }

        if (getRank(factionEntry) != ReputationRank.Exalted && !hasMaximumRenownReputation(factionEntry)) {
            return false;
        }

        var paragonReputation = global.getDB2Mgr().GetParagonReputation(factionEntry.ParagonFactionID);

        if (paragonReputation == null) {
            return false;
        }

        var quest = global.getObjectMgr().getQuestTemplate((int) paragonReputation.questID);

        if (quest == null) {
            return false;
        }

        return player.getLevel() >= player.getQuestMinLevel(quest);
    }
}
