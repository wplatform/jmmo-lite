package com.github.azeroth.game.dungeonfinding;


import com.github.azeroth.game.domain.condition.DisableType;
import com.github.azeroth.game.domain.player.AccessRequirement;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.group.PlayerGroup;
import com.github.azeroth.game.networking.packet.RideType;
import com.github.azeroth.game.networking.packet.rideTicket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.random;


public class LFGManager {
    private final HashMap<Byte, LFGQueue> queuesStore = new HashMap<Byte, LFGQueue>(); //< Queues

    private final MultiMap<Byte, Integer> cachedDungeonMapStore = new MultiMap<Byte, Integer>(); //< Stores all dungeons by groupType
    // Reward System

    private final MultiMap<Integer, LfgReward> rewardMapStore = new MultiMap<Integer, LfgReward>(); //< Stores rewards for random dungeons
    private final HashMap<Integer, LFGDungeonData> lfgDungeonStore = new HashMap<Integer, LFGDungeonData>();

    // Rolecheck - Proposal - Vote Kicks
    private final HashMap<ObjectGuid, LfgRoleCheck> roleChecksStore = new HashMap<ObjectGuid, LfgRoleCheck>(); //< Current Role checks
    private final HashMap<Integer, LfgProposal> proposalsStore = new HashMap<Integer, LfgProposal>(); //< Current Proposals
    private final HashMap<ObjectGuid, LfgPlayerBoot> bootsStore = new HashMap<ObjectGuid, LfgPlayerBoot>(); //< Current player kicks
    private final HashMap<ObjectGuid, LFGPlayerData> playersStore = new HashMap<ObjectGuid, LFGPlayerData>(); //< Player data
    private final HashMap<ObjectGuid, LFGGroupData> groupsStore = new HashMap<ObjectGuid, LFGGroupData>(); //< Group data

    // General variables
    private int m_QueueTimer; //< used to check interval of update
    private int m_lfgProposalId; //< used as internal counter for proposals
    private LfgOptions m_options = LfgOptions.values()[0]; //< Stores config options

    private LFGManager() {
        m_lfgProposalId = 1;
        m_options = LfgOptions.forValue(ConfigMgr.GetDefaultValue("DungeonFinder.OptionsMask", 1));

        new LFGPlayerScript();
        new LFGGroupScript();
    }

    public final String concatenateDungeons(ArrayList<Integer> dungeons) {
        StringBuilder dungeonstr = new StringBuilder();

        if (!dungeons.isEmpty()) {
            for (var id : dungeons) {
                if (dungeonstr.capacity() != 0) {
                    dungeonstr.append(String.format(", %1$s", id));
                } else {
                    dungeonstr.append(String.format("%1$s", id));
                }
            }
        }

        return dungeonstr.toString();
    }

    public final void _LoadFromDB(SQLFields field, ObjectGuid guid) {
        if (field == null) {
            return;
        }

        if (!guid.isParty()) {
            return;
        }

        setLeader(guid, ObjectGuid.create(HighGuid.Player, field.<Long>Read(0)));

        var dungeon = field.<Integer>Read(18);
        var state = LfgState.forValue(field.<Byte>Read(19));

        if (dungeon == 0 || state == 0) {
            return;
        }

        setDungeon(guid, dungeon);

        switch (state) {
            case Dungeon:
            case FinishedDungeon:
                setState(guid, state);

                break;
            default:
                break;
        }
    }

    public final void loadRewards() {
        var oldMSTime = System.currentTimeMillis();

        rewardMapStore.clear();

        // ORDER BY is very important for GetRandomDungeonReward!
        var result = DB.World.query("SELECT dungeonId, maxLevel, firstQuestId, otherQuestId FROM lfg_dungeon_rewards ORDER BY dungeonId, maxLevel ASC");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 lfg dungeon rewards. DB table `lfg_dungeon_rewards` is empty!");

            return;
        }

        int count = 0;

        do {
            var dungeonId = result.<Integer>Read(0);
            int maxLevel = result.<Byte>Read(1);
            var firstQuestId = result.<Integer>Read(2);
            var otherQuestId = result.<Integer>Read(3);

            if (getLFGDungeonEntry(dungeonId) == 0) {
                Logs.SQL.error("Dungeon {0} specified in table `lfg_dungeon_rewards` does not exist!", dungeonId);

                continue;
            }

            if (maxLevel == 0 || maxLevel > WorldConfig.getIntValue(WorldCfg.MaxPlayerLevel)) {
                Logs.SQL.error("Level {0} specified for dungeon {1} in table `lfg_dungeon_rewards` can never be reached!", maxLevel, dungeonId);
                maxLevel = WorldConfig.getUIntValue(WorldCfg.MaxPlayerLevel);
            }

            if (firstQuestId == 0 || global.getObjectMgr().getQuestTemplate(firstQuestId) == null) {
                Logs.SQL.error("First quest {0} specified for dungeon {1} in table `lfg_dungeon_rewards` does not exist!", firstQuestId, dungeonId);

                continue;
            }

            if (otherQuestId != 0 && global.getObjectMgr().getQuestTemplate(otherQuestId) == null) {
                Logs.SQL.error("Other quest {0} specified for dungeon {1} in table `lfg_dungeon_rewards` does not exist!", otherQuestId, dungeonId);
                otherQuestId = 0;
            }

            rewardMapStore.add(dungeonId, new LfgReward(maxLevel, firstQuestId, otherQuestId));
            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} lfg dungeon rewards in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }


    public final void loadLFGDungeons() {
        loadLFGDungeons(false);
    }

    public final void loadLFGDungeons(boolean reload) {
        var oldMSTime = System.currentTimeMillis();

        lfgDungeonStore.clear();

        // Initialize Dungeon map with data from dbcs
        for (var dungeon : CliDB.LFGDungeonsStorage.values()) {
            if (global.getDB2Mgr().GetMapDifficultyData((int) dungeon.mapID, dungeon.difficultyID) == null) {
                continue;
            }

            switch (dungeon.typeID) {
                case LfgType.Dungeon:
                case LfgType.Raid:
                case LfgType.Random:
                case LfgType.Zone:
                    lfgDungeonStore.put(dungeon.id, new LFGDungeonData(dungeon));

                    break;
            }
        }

        // Fill teleport locations from DB
        var result = DB.World.query("SELECT dungeonId, position_x, position_y, position_z, orientation, requiredItemLevel FROM lfg_dungeon_template");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 lfg dungeon templates. DB table `lfg_dungeon_template` is empty!");

            return;
        }

        int count = 0;

        do {
            var dungeonId = result.<Integer>Read(0);

            if (!lfgDungeonStore.containsKey(dungeonId)) {
                Logs.SQL.error("table `lfg_entrances` contains coordinates for wrong dungeon {0}", dungeonId);

                continue;
            }

            var data = lfgDungeonStore.get(dungeonId);
            data.x = result.<Float>Read(1);
            data.y = result.<Float>Read(2);
            data.z = result.<Float>Read(3);
            data.o = result.<Float>Read(4);
            data.requiredItemLevel = result.<SHORT>Read(5);

            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} lfg dungeon templates in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));

        // Fill all other teleport coords from areatriggers
        for (var pair : lfgDungeonStore.entrySet()) {
            var dungeon = pair.getValue();

            // No teleport coords in database, load from areatriggers
            if (dungeon.type != LfgType.random && dungeon.x == 0.0f && dungeon.y == 0.0f && dungeon.z == 0.0f) {
                var at = global.getObjectMgr().getMapEntranceTrigger(dungeon.map);

                if (at == null) {
                    Log.outError(LogFilter.Lfg, "LoadLFGDungeons: Failed to load dungeon {0} (Id: {1}), cant find areatrigger for map {2}", dungeon.name, dungeon.id, dungeon.map);

                    continue;
                }

                dungeon.map = at.target_mapId;
                dungeon.x = at.target_X;
                dungeon.y = at.target_Y;
                dungeon.z = at.target_Z;
                dungeon.o = at.target_Orientation;
            }

            if (dungeon.type != LfgType.random) {
                cachedDungeonMapStore.add((byte) dungeon.group, dungeon.id);
            }

            cachedDungeonMapStore.add((byte) 0, dungeon.id);
        }

        if (reload) {
            cachedDungeonMapStore.clear();
        }
    }

    public final void update(int diff) {
        if (!isOptionEnabled(LfgOptions.EnableDungeonFinder.getValue() | LfgOptions.EnableRaidBrowser.getValue())) {
            return;
        }

        var currTime = gameTime.GetGameTime();

        // Remove obsolete role checks
        for (var pairCheck : roleChecksStore.entrySet()) {
            var roleCheck = pairCheck.getValue();

            if (currTime < roleCheck.cancelTime) {
                continue;
            }

            roleCheck.state = LfgRoleCheckState.MissingRole;

            for (var pairRole : roleCheck.roles) {
                var guid = pairRole.key;
                restoreState(guid, "Remove Obsolete RoleCheck");
                sendLfgRoleCheckUpdate(guid, roleCheck);

                if (guid == roleCheck.leader) {
                    sendLfgJoinResult(guid, new LfgJoinResultData(LfgJoinResult.RoleCheckFailed, LfgRoleCheckState.MissingRole));
                }
            }

            restoreState(pairCheck.getKey(), "Remove Obsolete RoleCheck");
            roleChecksStore.remove(pairCheck.getKey());
        }

        // Remove obsolete proposals
        for (var removePair : proposalsStore.ToList()) {
            if (removePair.value.cancelTime < currTime) {
                removeProposal(removePair, LfgUpdateType.ProposalFailed);
            }
        }

        // Remove obsolete kicks
        for (var itBoot : bootsStore.entrySet()) {
            var boot = itBoot.getValue();

            if (boot.cancelTime < currTime) {
                boot.inProgress = false;

                for (var itVotes : boot.votes) {
                    var pguid = itVotes.key;

                    if (pguid != boot.victim) {
                        sendLfgBootProposalUpdate(pguid, boot);
                    }
                }

                setVoteKick(itBoot.getKey(), false);
                bootsStore.remove(itBoot.getKey());
            }
        }

        var lastProposalId = m_lfgProposalId;

        // Check if a proposal can be formed with the new groups being added
        for (var it : queuesStore.entrySet()) {
            var newProposals = it.getValue().findGroups();

            if (newProposals != 0) {
                Log.outDebug(LogFilter.Lfg, "Update: Found {0} new groups in queue {1}", newProposals, it.getKey());
            }
        }

        if (lastProposalId != m_lfgProposalId) {
            // FIXME lastProposalId ? lastProposalId +1 ?
            for (var itProposal : proposalsStore.SkipWhile(p -> p.key == m_lfgProposalId)) {
                var proposalId = itProposal.key;
                var proposal = proposalsStore.get(proposalId);

                var guid = ObjectGuid.Empty;

                for (var itPlayers : proposal.players.entrySet()) {
                    guid = itPlayers.getKey();
                    setState(guid, LfgState.Proposal);
                    var gguid = getGroup(guid);

                    if (!gguid.isEmpty()) {
                        setState(gguid, LfgState.Proposal);
                        sendLfgUpdateStatus(guid, new LfgUpdateData(LfgUpdateType.ProposalBegin, getSelectedDungeons(guid)), true);
                    } else {
                        sendLfgUpdateStatus(guid, new LfgUpdateData(LfgUpdateType.ProposalBegin, getSelectedDungeons(guid)), false);
                    }

                    sendLfgUpdateProposal(guid, proposal);
                }

                if (proposal.state == LfgProposalState.success) {
                    updateProposal(proposalId, guid, true);
                }
            }
        }

        // Update all players status queue info
        if (m_QueueTimer > SharedConst.LFGQueueUpdateInterval) {
            m_QueueTimer = 0;

            for (var it : queuesStore.entrySet()) {
                it.getValue().updateQueueTimers(it.getKey(), currTime);
            }
        } else {
            m_QueueTimer += diff;
        }
    }

    public final void joinLfg(Player player, LfgRoles roles, ArrayList<Integer> dungeons) {
        if (!player || player.getSession() == null || dungeons.isEmpty()) {
            return;
        }

        // Sanitize input roles
        roles = LfgRoles.forValue(roles.getValue() & LfgRoles.Any.getValue());
        roles = filterClassRoles(player, roles);

        // At least 1 role must be selected
        if ((roles.getValue() & (LfgRoles.Tank.getValue() | LfgRoles.healer.getValue().getValue() | LfgRoles.damage.getValue().getValue()).getValue()) == 0) {
            return;
        }

        var grp = player.getGroup();
        var guid = player.getGUID();
        var gguid = grp ? grp.getGUID() : guid;
        LfgJoinResultData joinData = new LfgJoinResultData();
        ArrayList<ObjectGuid> players = new ArrayList<>();
        int rDungeonId = 0;
        var isContinue = grp && grp.isLFGGroup() && getState(gguid) != LfgState.FinishedDungeon;

        // Do not allow to change dungeon in the middle of a current dungeon
        if (isContinue) {
            dungeons.clear();
            dungeons.add(getDungeon(gguid));
        }

        // Already in queue?
        var state = getState(gguid);

        if (state == LfgState.queued) {
            var queue = getQueue(gguid);
            queue.removeFromQueue(gguid);
        }

        // Check player or group member restrictions
        if (!player.getSession().hasPermission(RBACPermissions.JoinDungeonFinder)) {
            joinData.result = LfgJoinResult.NoSlots;
        } else if (player.getInBattleground() || player.getInArena() || player.inBattlegroundQueue()) {
            joinData.result = LfgJoinResult.CantUseDungeons;
        } else if (player.hasAura(SharedConst.LFGSpellDungeonDeserter)) {
            joinData.result = LfgJoinResult.DeserterPlayer;
        } else if (!isContinue && player.hasAura(SharedConst.LFGSpellDungeonCooldown)) {
            joinData.result = LfgJoinResult.RandomCooldownPlayer;
        } else if (dungeons.isEmpty()) {
            joinData.result = LfgJoinResult.NoSlots;
        } else if (player.hasAura(9454)) // check Freeze debuff
        {
            joinData.result = LfgJoinResult.NoSlots;
        } else if (grp) {
            if (grp.getMembersCount() > MapDefine.MaxGroupSize) {
                joinData.result = LfgJoinResult.TooManyMembers;
            } else {
                byte memberCount = 0;

                for (var refe = grp.getFirstMember(); refe != null && joinData.result == LfgJoinResult.Ok; refe = refe.next()) {
                    var plrg = refe.getSource();

                    if (plrg) {
                        if (!plrg.getSession().hasPermission(RBACPermissions.JoinDungeonFinder)) {
                            joinData.result = LfgJoinResult.NoLfgObject;
                        }

                        if (plrg.hasAura(SharedConst.LFGSpellDungeonDeserter)) {
                            joinData.result = LfgJoinResult.DeserterParty;
                        } else if (!isContinue && plrg.hasAura(SharedConst.LFGSpellDungeonCooldown)) {
                            joinData.result = LfgJoinResult.RandomCooldownParty;
                        } else if (plrg.getInBattleground() || plrg.getInArena() || plrg.inBattlegroundQueue()) {
                            joinData.result = LfgJoinResult.CantUseDungeons;
                        } else if (plrg.hasAura(9454)) // check Freeze debuff
                        {
                            joinData.result = LfgJoinResult.NoSlots;
                            joinData.playersMissingRequirement.add(plrg.getName());
                        }

                        ++memberCount;
                        players.add(plrg.getGUID());
                    }
                }

                if (joinData.result == LfgJoinResult.Ok && memberCount != grp.getMembersCount()) {
                    joinData.result = LfgJoinResult.MembersNotPresent;
                }
            }
        } else {
            players.add(player.getGUID());
        }

        // Check if all dungeons are valid
        var isRaid = false;

        if (joinData.result == LfgJoinResult.Ok) {
            var isDungeon = false;

            for (var it : dungeons) {
                if (joinData.result != LfgJoinResult.Ok) {
                    break;
                }

                var type = getDungeonType(it);

                switch (type) {
                    case Random:
                        if (dungeons.size() > 1) // Only allow 1 random dungeon
                        {
                            joinData.result = LfgJoinResult.InvalidSlot;
                        } else {
                            rDungeonId = dungeons.get(0);
                        }

                    case Dungeon:
                        if (isRaid) {
                            joinData.result = LfgJoinResult.MismatchedSlots;
                        }

                        isDungeon = true;

                        break;
                    case Raid:
                        if (isDungeon) {
                            joinData.result = LfgJoinResult.MismatchedSlots;
                        }

                        isRaid = true;

                        break;
                    default:
                        Log.outError(LogFilter.Lfg, "Wrong dungeon type {0} for dungeon {1}", type, it);
                        joinData.result = LfgJoinResult.InvalidSlot;

                        break;
                }
            }

            // it could be changed
            if (joinData.result == LfgJoinResult.Ok) {
                // Expand random dungeons and check restrictions
                if (rDungeonId != 0) {
                    dungeons = getDungeonsByRandom(rDungeonId);
                }

                // if we have lockmap then there are no compatible dungeons
                getCompatibleDungeons(dungeons, players, joinData.lockmap, joinData.playersMissingRequirement, isContinue);

                if (dungeons.isEmpty()) {
                    joinData.result = LfgJoinResult.NoSlots;
                }
            }
        }

        // Can't join. Send result
        if (joinData.result != LfgJoinResult.Ok) {
            Log.outDebug(LogFilter.Lfg, "Join: [{0}] joining with {1} members. result: {2}", guid, grp ? grp.getMembersCount() : 1, joinData.result);

            if (!dungeons.isEmpty()) // Only should show lockmap when have no dungeons available
            {
                joinData.lockmap.clear();
            }

            player.getSession().sendLfgJoinResult(joinData);

            return;
        }

        if (isRaid) {
            Log.outDebug(LogFilter.Lfg, "Join: [{0}] trying to join raid browser and it's disabled.", guid);

            return;
        }

        RideTicket ticket = new rideTicket();
        ticket.requesterGuid = guid;
        ticket.id = getQueueId(gguid);
        ticket.type = RideType.Lfg;
        ticket.time = gameTime.GetGameTime();

        var debugNames = "";

        if (grp) // Begin rolecheck
        {
            // Create new rolecheck
            LfgRoleCheck roleCheck = new LfgRoleCheck();
            roleCheck.cancelTime = gameTime.GetGameTime() + SharedConst.LFGTimeRolecheck;
            roleCheck.state = LfgRoleCheckState.Initialiting;
            roleCheck.leader = guid;
            roleCheck.dungeons = dungeons;
            roleCheck.rDungeonId = rDungeonId;

            roleChecksStore.put(gguid, roleCheck);

            if (rDungeonId != 0) {
                dungeons.clear();
                dungeons.add(rDungeonId);
            }

            setState(gguid, LfgState.Rolecheck);
            // Send update to player
            LfgUpdateData updateData = new LfgUpdateData(LfgUpdateType.JoinQueue, dungeons);

            for (var refe = grp.getFirstMember(); refe != null; refe = refe.next()) {
                var plrg = refe.getSource();

                if (plrg) {
                    var pguid = plrg.getGUID();
                    plrg.getSession().sendLfgUpdateStatus(updateData, true);
                    setState(pguid, LfgState.Rolecheck);
                    setTicket(pguid, ticket);

                    if (!isContinue) {
                        setSelectedDungeons(pguid, dungeons);
                    }

                    roleCheck.roles.put(pguid, 0);

                    if (!StringUtil.isEmpty(debugNames)) {
                        debugNames += ", ";
                    }

                    debugNames += plrg.getName();
                }
            }

            // Update leader role
            updateRoleCheck(gguid, guid, roles);
        } else // Add player to queue
        {
            HashMap<ObjectGuid, LfgRoles> rolesMap = new HashMap<ObjectGuid, LfgRoles>();
            rolesMap.put(guid, roles);
            var queue = getQueue(guid);
            queue.addQueueData(guid, gameTime.GetGameTime(), dungeons, rolesMap);

            if (!isContinue) {
                if (rDungeonId != 0) {
                    dungeons.clear();
                    dungeons.add(rDungeonId);
                }

                setSelectedDungeons(guid, dungeons);
            }

            // Send update to player
            setTicket(guid, ticket);
            setRoles(guid, roles);
            player.getSession().sendLfgUpdateStatus(new LfgUpdateData(LfgUpdateType.JoinQueueInitial, dungeons), false);
            setState(gguid, LfgState.queued);
            player.getSession().sendLfgUpdateStatus(new LfgUpdateData(LfgUpdateType.AddedToQueue, dungeons), false);
            player.getSession().sendLfgJoinResult(joinData);
            debugNames += player.getName();
        }

        StringBuilder o = new StringBuilder();
        o.append(String.format("Join: [%1$s] joined (%2$s%3$s) Members: %4$s. dungeons (%5$s): ", guid, (grp ? "group" : "player"), debugNames, dungeons.size(), concatenateDungeons(dungeons)));
        Log.outDebug(LogFilter.Lfg, o.toString());
    }


    public final void leaveLfg(ObjectGuid guid) {
        leaveLfg(guid, false);
    }

    public final void leaveLfg(ObjectGuid guid, boolean disconnected) {
        Log.outDebug(LogFilter.Lfg, "LeaveLfg: [{0}]", guid);

        var gguid = guid.isParty() ? guid : getGroup(guid);
        var state = getState(guid);

        switch (state) {
            case Queued:
                if (!gguid.isEmpty()) {
                    var newState = LfgState.NONE;
                    var oldState = getOldState(gguid);

                    // Set the new state to LFG_STATE_DUNGEON/LFG_STATE_FINISHED_DUNGEON if the group is already in a dungeon
                    // This is required in case a LFG group vote-kicks a player in a dungeon, queues, then leaves the queue (maybe to queue later again)
                    var group = global.getGroupMgr().getGroupByGUID(gguid);

                    if (group != null) {
                        if (group.isLFGGroup() && getDungeon(gguid) != 0 && (oldState == LfgState.Dungeon || oldState == LfgState.FinishedDungeon)) {
                            newState = oldState;
                        }
                    }

                    var queue = getQueue(gguid);
                    queue.removeFromQueue(gguid);
                    setState(gguid, newState);
                    var players = getPlayers(gguid);

                    for (var it : players) {
                        setState(it, newState);
                        sendLfgUpdateStatus(it, new LfgUpdateData(LfgUpdateType.RemovedFromQueue), true);
                    }
                } else {
                    sendLfgUpdateStatus(guid, new LfgUpdateData(LfgUpdateType.RemovedFromQueue), false);
                    var queue = getQueue(guid);
                    queue.removeFromQueue(guid);
                    setState(guid, LfgState.NONE);
                }

                break;
            case Rolecheck:
                if (!gguid.isEmpty()) {
                    updateRoleCheck(gguid); // No player to update role = LFG_ROLECHECK_ABORTED
                }

                break;
            case Proposal: {
                // Remove from Proposals
                java.util.Map.entry<Integer, LfgProposal> it = new KeyValuePair<Integer, LfgProposal>();
                var pguid = Objects.equals(gguid, guid) ? getLeader(gguid) : guid;

                for (var test : proposalsStore.entrySet()) {
                    it = test;
                    var itPlayer = it.getValue().players.get(pguid);

                    if (itPlayer != null) {
                        // Mark the player/leader of group who left as didn't accept the proposal
                        itPlayer.accept = LfgAnswer.Deny;

                        break;
                    }
                }

                // Remove from queue - if proposal is found, RemoveProposal will call RemoveFromQueue
                if (it.getValue() != null) {
                    removeProposal(it, LfgUpdateType.ProposalDeclined);
                }

                break;
            }
            case None:
            case Raidbrowser:
                break;
            case Dungeon:
            case FinishedDungeon:
                if (ObjectGuid.opNotEquals(guid, gguid) && !disconnected) // Player
                {
                    setState(guid, LfgState.NONE);
                }

                break;
        }
    }

    public final RideTicket getTicket(ObjectGuid guid) {
        var palyerData = playersStore.get(guid);

        if (palyerData != null) {
            return palyerData.getTicket();
        }

        return null;
    }


    public final void updateRoleCheck(ObjectGuid gguid, ObjectGuid guid) {
        updateRoleCheck(gguid, guid, LfgRoles.NONE);
    }

    public final void updateRoleCheck(ObjectGuid gguid) {
        updateRoleCheck(gguid, null, LfgRoles.NONE);
    }

    public final void updateRoleCheck(ObjectGuid gguid, ObjectGuid guid, LfgRoles roles) {
        if (gguid.isEmpty()) {
            return;
        }

        HashMap<ObjectGuid, LfgRoles> check_roles;
        var roleCheck = roleChecksStore.get(gguid);

        if (roleCheck == null) {
            return;
        }

        // Sanitize input roles
        roles = LfgRoles.forValue(roles.getValue() & LfgRoles.Any.getValue());

        if (!guid.isEmpty()) {
            var player = global.getObjAccessor().findPlayer(guid);

            if (player != null) {
                roles = filterClassRoles(player, roles);
            } else {
                return;
            }
        }

        var sendRoleChosen = roleCheck.state != LfgRoleCheckState.Default && !guid.isEmpty();

        if (guid.isEmpty()) {
            roleCheck.state = LfgRoleCheckState.aborted;
        } else if (roles.getValue() < LfgRoles.Tank.getValue()) // Player selected no role.
        {
            roleCheck.state = LfgRoleCheckState.NoRole;
        } else {
            roleCheck.roles[guid] = roles;

            // Check if all players have selected a role
            var done = false;

            for (var rolePair : roleCheck.roles) {
                if (rolePair.value != LfgRoles.NONE) {
                    continue;
                }

                done = true;
            }

            if (done) {
                // use temporal var to check roles, CheckGroupRoles modifies the roles
                check_roles = roleCheck.roles;
                roleCheck.state = checkGroupRoles(check_roles) ? LfgRoleCheckState.Finished : LfgRoleCheckState.WrongRoles;
            }
        }

        ArrayList<Integer> dungeons = new ArrayList<>();

        if (roleCheck.rDungeonId != 0) {
            dungeons.add(roleCheck.rDungeonId);
        } else {
            dungeons = roleCheck.dungeons;
        }

        LfgJoinResultData joinData = new LfgJoinResultData(LfgJoinResult.RoleCheckFailed, roleCheck.state);

        for (var it : roleCheck.roles) {
            var pguid = it.key;

            if (sendRoleChosen) {
                sendLfgRoleChosen(pguid, guid, roles);
            }

            sendLfgRoleCheckUpdate(pguid, roleCheck);

            switch (roleCheck.state) {
                case LfgRoleCheckState.Initialiting:
                    continue;
                case LfgRoleCheckState.Finished:
                    setState(pguid, LfgState.queued);
                    setRoles(pguid, it.value);
                    sendLfgUpdateStatus(pguid, new LfgUpdateData(LfgUpdateType.AddedToQueue, dungeons), true);

                    break;
                default:
                    if (roleCheck.leader == pguid) {
                        sendLfgJoinResult(pguid, joinData);
                    }

                    sendLfgUpdateStatus(pguid, new LfgUpdateData(LfgUpdateType.RolecheckFailed), true);
                    restoreState(pguid, "Rolecheck Failed");

                    break;
            }
        }

        if (roleCheck.state == LfgRoleCheckState.Finished) {
            setState(gguid, LfgState.queued);
            var queue = getQueue(gguid);
            queue.addQueueData(gguid, gameTime.GetGameTime(), roleCheck.dungeons, roleCheck.roles);
            roleChecksStore.remove(gguid);
        } else if (roleCheck.state != LfgRoleCheckState.Initialiting) {
            restoreState(gguid, "Rolecheck Failed");
            roleChecksStore.remove(gguid);
        }
    }

    public final boolean checkGroupRoles(HashMap<ObjectGuid, LfgRoles> groles) {
        if (groles.isEmpty()) {
            return false;
        }

        byte damage = 0;
        byte tank = 0;
        byte healer = 0;

        ArrayList<ObjectGuid> keys = new ArrayList<ObjectGuid>(groles.keySet());

        for (var i = 0; i < keys.size(); i++) {
            var role = LfgRoles.forValue(groles.get(keys.get(i)).getValue() & ~LfgRoles.leader.getValue());

            if (role == LfgRoles.NONE) {
                return false;
            }

            if (role.hasFlag(LfgRoles.damage)) {
                if (role != LfgRoles.damage) {
                    groles.put(keys.get(i), groles.get(keys.get(i)) - LfgRoles.damage);

                    if (checkGroupRoles(groles)) {
                        return true;
                    }

                    groles.put(keys.get(i), groles.get(keys.get(i)) + (byte) LfgRoles.damage.getValue());
                } else if (damage == SharedConst.LFGDPSNeeded) {
                    return false;
                } else {
                    damage++;
                }
            }

            if (role.hasFlag(LfgRoles.healer)) {
                if (role != LfgRoles.healer) {
                    groles.put(keys.get(i), groles.get(keys.get(i)) - LfgRoles.healer);

                    if (checkGroupRoles(groles)) {
                        return true;
                    }

                    groles.put(keys.get(i), groles.get(keys.get(i)) + (byte) LfgRoles.healer.getValue());
                } else if (healer == SharedConst.LFGHealersNeeded) {
                    return false;
                } else {
                    healer++;
                }
            }

            if (role.hasFlag(LfgRoles.Tank)) {
                if (role != LfgRoles.Tank) {
                    groles.put(keys.get(i), groles.get(keys.get(i)) - LfgRoles.Tank);

                    if (checkGroupRoles(groles)) {
                        return true;
                    }

                    groles.put(keys.get(i), groles.get(keys.get(i)) + (byte) LfgRoles.Tank.getValue());
                } else if (tank == SharedConst.LFGTanksNeeded) {
                    return false;
                } else {
                    tank++;
                }
            }
        }

        return (tank + healer + damage) == (byte) groles.size();
    }

    public final int addProposal(LfgProposal proposal) {
        proposal.id = ++m_lfgProposalId;
        proposalsStore.put(m_lfgProposalId, proposal);

        return m_lfgProposalId;
    }

    public final void updateProposal(int proposalId, ObjectGuid guid, boolean accept) {
        // Check if the proposal exists
        var proposal = proposalsStore.get(proposalId);

        if (proposal == null) {
            return;
        }

        // Check if proposal have the current player
        var player = proposal.players.get(guid);

        if (player == null) {
            return;
        }

        player.accept = LfgAnswer.forValue((int) accept);

        Log.outDebug(LogFilter.Lfg, "UpdateProposal: Player [{0}] of proposal {1} selected: {2}", guid, proposalId, accept);

        if (!accept) {
            removeProposal(new KeyValuePair<Integer, LfgProposal>(proposalId, proposal), LfgUpdateType.ProposalDeclined);

            return;
        }

        // check if all have answered and reorder players (leader first)
        var allAnswered = true;

        for (var itPlayers : proposal.players) {
            if (itPlayers.value.accept != LfgAnswer.Agree) // No answer (-1) or not accepted (0)
            {
                allAnswered = false;
            }
        }

        if (!allAnswered) {
            for (var it : proposal.players) {
                sendLfgUpdateProposal(it.key, proposal);
            }

            return;
        }

        var sendUpdate = proposal.state != LfgProposalState.success;
        proposal.state = LfgProposalState.success;
        var joinTime = gameTime.GetGameTime();

        var queue = getQueue(guid);
        LfgUpdateData updateData = new LfgUpdateData(LfgUpdateType.GroupFound);

        for (var it : proposal.players) {
            var pguid = it.key;
            var gguid = it.value.group;
            var dungeonId = getSelectedDungeons(pguid).get(0);
            int waitTime;

            if (sendUpdate) {
                sendLfgUpdateProposal(pguid, proposal);
            }

            if (!gguid.IsEmpty) {
                waitTime = (int) ((joinTime - queue.getJoinTime(gguid)) / time.InMilliseconds);
                sendLfgUpdateStatus(pguid, updateData, false);
            } else {
                waitTime = (int) ((joinTime - queue.getJoinTime(pguid)) / time.InMilliseconds);
                sendLfgUpdateStatus(pguid, updateData, false);
            }

            updateData.updateType = LfgUpdateType.RemovedFromQueue;
            sendLfgUpdateStatus(pguid, updateData, true);
            sendLfgUpdateStatus(pguid, updateData, false);

            // Update timers
            var role = getRoles(pguid);
            role = LfgRoles.forValue(role.getValue() & ~LfgRoles.leader.getValue());

            switch (role) {
                case Damage:
                    queue.updateWaitTimeDps(waitTime, dungeonId);

                    break;
                case Healer:
                    queue.updateWaitTimeHealer(waitTime, dungeonId);

                    break;
                case Tank:
                    queue.updateWaitTimeTank(waitTime, dungeonId);

                    break;
                default:
                    queue.updateWaitTimeAvg(waitTime, dungeonId);

                    break;
            }

            // Store the number of players that were present in group when joining RFD, used for achievement purposes
            var player = global.getObjAccessor().findConnectedPlayer(pguid);

            if (player != null) {
                var group = player.getGroup();

                if (group != null) {
                    playersStore.get(pguid).setNumberOfPartyMembersAtJoin((byte) group.getMembersCount());
                }
            }

            setState(pguid, LfgState.Dungeon);
        }

        // Remove players/groups from Queue
        for (var it : proposal.queues) {
            queue.removeFromQueue(it);
        }

        makeNewGroup(proposal);
        proposalsStore.remove(proposalId);
    }

    public final void initBoot(ObjectGuid gguid, ObjectGuid kicker, ObjectGuid victim, String reason) {
        setVoteKick(gguid, true);

        var boot = bootsStore.get(gguid);
        boot.inProgress = true;
        boot.cancelTime = gameTime.GetGameTime() + SharedConst.LFGTimeBoot;
        boot.reason = reason;
        boot.victim = victim;

        var players = getPlayers(gguid);

        // Set votes
        for (var guid : players) {
            boot.votes.put(guid, LfgAnswer.Pending);
        }

        boot.votes.put(victim, LfgAnswer.Deny); // Victim auto vote NO
        boot.votes.put(kicker, LfgAnswer.Agree); // Kicker auto vote YES

        // Notify players
        for (var it : players) {
            sendLfgBootProposalUpdate(it, boot);
        }
    }

    public final void updateBoot(ObjectGuid guid, boolean accept) {
        var gguid = getGroup(guid);

        if (gguid.isEmpty()) {
            return;
        }

        var boot = bootsStore.get(gguid);

        if (boot == null) {
            return;
        }

        if (boot.votes[guid] != LfgAnswer.Pending) // Cheat check: Player can't vote twice
        {
            return;
        }

        boot.votes[guid] = LfgAnswer.forValue((int) accept);

        byte agreeNum = 0;
        byte denyNum = 0;


        for (var(_, answer) : boot.votes) {
            switch (answer) {
                case LfgAnswer.Pending:
                    break;
                case LfgAnswer.Agree:
                    ++agreeNum;

                    break;
                case LfgAnswer.Deny:
                    ++denyNum;

                    break;
            }
        }

        // if we don't have enough votes (agree or deny) do nothing
        if (agreeNum < SharedConst.LFGKickVotesNeeded && (boot.votes.Count - denyNum) >= SharedConst.LFGKickVotesNeeded) {
            return;
        }

        // Send update info to all players
        boot.inProgress = false;

        for (var itVotes : boot.votes) {
            var pguid = itVotes.key;

            if (pguid != boot.victim) {
                sendLfgBootProposalUpdate(pguid, boot);
            }
        }

        setVoteKick(gguid, false);

        if (agreeNum == SharedConst.LFGKickVotesNeeded) // Vote passed - Kick player
        {
            var group = global.getGroupMgr().getGroupByGUID(gguid);

            if (group) {
                player.removeFromGroup(group, boot.victim, RemoveMethod.KickLFG);
            }

            decreaseKicksLeft(gguid);
        }

        bootsStore.remove(gguid);
    }


    public final void teleportPlayer(Player player, boolean outt) {
        teleportPlayer(player, outt, false);
    }

    public final void teleportPlayer(Player player, boolean outt, boolean fromOpcode) {
        LFGDungeonData dungeon = null;
        var group = player.getGroup();

        if (group && group.isLFGGroup()) {
            dungeon = getLFGDungeon(getDungeon(group.getGUID()));
        }

        if (dungeon == null) {
            Log.outDebug(LogFilter.Lfg, "TeleportPlayer: Player {0} not in group/lfggroup or dungeon not found!", player.getName());
            player.getSession().sendLfgTeleportError(LfgTeleportResult.NoReturnLocation);

            return;
        }

        if (outt) {
            Log.outDebug(LogFilter.Lfg, "TeleportPlayer: Player {0} is being teleported out. Current Map {1} - Expected Map {2}", player.getName(), player.getLocation().getMapId(), dungeon.map);

            if (player.getLocation().getMapId() == dungeon.map) {
                player.teleportToBGEntryPoint();
            }

            return;
        }

        var error = LfgTeleportResult.NONE;

        if (!player.isAlive()) {
            error = LfgTeleportResult.Dead;
        } else if (player.isFalling() || player.hasUnitState(UnitState.Jumping)) {
            error = LfgTeleportResult.Falling;
        } else if (player.isMirrorTimerActive(MirrorTimerType.Fatigue)) {
            error = LfgTeleportResult.Exhaustion;
        } else if (player.getVehicle1()) {
            error = LfgTeleportResult.OnTransport;
        } else if (!player.getCharmedGUID().isEmpty()) {
            error = LfgTeleportResult.ImmuneToSummons;
        } else if (player.hasAura(9454)) // check Freeze debuff
        {
            error = LfgTeleportResult.NoReturnLocation;
        } else if (player.getLocation().getMapId() != dungeon.map) // Do not teleport players in dungeon to the entrance
        {
            var mapid = dungeon.map;
            var x = dungeon.x;
            var y = dungeon.y;
            var z = dungeon.z;
            var orientation = dungeon.o;

            if (!fromOpcode) {
                // Select a player inside to be teleported to
                for (var refe = group.getFirstMember(); refe != null; refe = refe.next()) {
                    var plrg = refe.getSource();

                    if (plrg && plrg != player && plrg.getLocation().getMapId() == dungeon.map) {
                        mapid = plrg.getLocation().getMapId();
                        x = plrg.getLocation().getX();
                        y = plrg.getLocation().getY();
                        z = plrg.getLocation().getZ();
                        orientation = plrg.getLocation().getO();

                        break;
                    }
                }
            }

            if (!player.getMap().isDungeon()) {
                player.setBattlegroundEntryPoint();
            }

            player.finishTaxiFlight();

            if (!player.teleportTo(mapid, x, y, z, orientation)) {
                error = LfgTeleportResult.NoReturnLocation;
            }
        } else {
            error = LfgTeleportResult.NoReturnLocation;
        }

        if (error != LfgTeleportResult.NONE) {
            player.getSession().sendLfgTeleportError(error);
        }

        Log.outDebug(LogFilter.Lfg, "TeleportPlayer: Player {0} is being teleported in to map {1} (x: {2}, y: {3}, z: {4}) Result: {5}", player.getName(), dungeon.map, dungeon.x, dungeon.y, dungeon.z, error);
    }

    public final void finishDungeon(ObjectGuid gguid, int dungeonId, Map currMap) {
        var gDungeonId = getDungeon(gguid);

        if (gDungeonId != dungeonId) {
            Log.outDebug(LogFilter.Lfg, String.format("Group %1$s finished dungeon %2$s but queued for %3$s. Ignoring", gguid, dungeonId, gDungeonId));

            return;
        }

        if (getState(gguid) == LfgState.FinishedDungeon) // Shouldn't happen. Do not reward multiple times
        {
            Log.outDebug(LogFilter.Lfg, String.format("Group %1$s already rewarded", gguid));

            return;
        }

        setState(gguid, LfgState.FinishedDungeon);

        var players = getPlayers(gguid);

        for (var guid : players) {
            if (getState(guid) == LfgState.FinishedDungeon) {
                Log.outDebug(LogFilter.Lfg, String.format("Group: %1$s, Player: %2$s already rewarded", gguid, guid));

                continue;
            }

            int rDungeonId = 0;
            var dungeons = getSelectedDungeons(guid);

            if (!dungeons.isEmpty()) {
                rDungeonId = dungeons.get(0);
            }

            setState(guid, LfgState.FinishedDungeon);

            // Give rewards only if its a random dungeon
            var dungeon = getLFGDungeon(rDungeonId);

            if (dungeon == null || (dungeon.type != LfgType.random && !dungeon.seasonal)) {
                Log.outDebug(LogFilter.Lfg, String.format("Group: %1$s, Player: %2$s dungeon %3$s is not random or seasonal", gguid, guid, rDungeonId));

                continue;
            }

            var player = global.getObjAccessor().findPlayer(guid);

            if (player == null) {
                Log.outDebug(LogFilter.Lfg, String.format("Group: %1$s, Player: %2$s not found in world", gguid, guid));

                continue;
            }

            if (player.getMap() != currMap) {
                Log.outDebug(LogFilter.Lfg, String.format("Group: %1$s, Player: %2$s is in a different map", gguid, guid));

                continue;
            }

            player.removeAura(SharedConst.LFGSpellDungeonCooldown);

            var dungeonDone = getLFGDungeon(dungeonId);
            var mapId = dungeonDone != null ? dungeonDone.map : 0;

            if (player.getLocation().getMapId() != mapId) {
                Log.outDebug(LogFilter.Lfg, String.format("Group: %1$s, Player: %2$s is in map %3$s and should be in %4$s to get reward", gguid, guid, player.getLocation().getMapId(), mapId));

                continue;
            }

            // Update achievements
            if (dungeon.difficulty == Difficulty.Heroic) {
                byte lfdRandomPlayers = 0;
                var numParty = playersStore.get(guid).getNumberOfPartyMembersAtJoin();

                if (numParty != 0) {
                    lfdRandomPlayers = (byte) (5 - numParty);
                } else {
                    lfdRandomPlayers = 4;
                }

                player.updateCriteria(CriteriaType.CompletedLFGDungeonWithStrangers, lfdRandomPlayers);
            }

            var reward = getRandomDungeonReward(rDungeonId, player.getLevel());

            if (reward == null) {
                continue;
            }

            var done = false;
            var quest = global.getObjectMgr().getQuestTemplate(reward.firstQuest);

            if (quest == null) {
                continue;
            }

            // if we can take the quest, means that we haven't done this kind of "run", IE: First Heroic Random of Day.
            if (player.canRewardQuest(quest, false)) {
                player.rewardQuest(quest, lootItemType.item, 0, null, false);
            } else {
                done = true;
                quest = global.getObjectMgr().getQuestTemplate(reward.otherQuest);

                if (quest == null) {
                    continue;
                }

                // we give reward without informing client (retail does this)
                player.rewardQuest(quest, lootItemType.item, 0, null, false);
            }

            // Give rewards
            var doneString = done ? "" : "not";
            Log.outDebug(LogFilter.Lfg, String.format("Group: %1$s, Player: %2$s done dungeon %3$s, %4$s previously done.", gguid, guid, getDungeon(gguid), doneString));
            LfgPlayerRewardData data = new LfgPlayerRewardData(dungeon.entry(), getDungeon(gguid, false), done, quest);
            player.getSession().sendLfgPlayerReward(data);
        }
    }

    public final LfgReward getRandomDungeonReward(int dungeon, int level) {
        LfgReward reward = null;
        var bounds = rewardMapStore.get(dungeon & 0x00FFFFFF);

        for (var rew : bounds) {
            reward = rew;

            // ordered properly at loading
            if (rew.maxLevel >= level) {
                break;
            }
        }

        return reward;
    }

    public final LfgType getDungeonType(int dungeonId) {
        var dungeon = getLFGDungeon(dungeonId);

        if (dungeon == null) {
            return LfgType.NONE;
        }

        return dungeon.type;
    }

    public final LfgState getState(ObjectGuid guid) {
        LfgState state;

        if (guid.isParty()) {
            if (!groupsStore.containsKey(guid)) {
                return LfgState.NONE;
            }

            state = groupsStore.get(guid).getState();
        } else {
            addPlayerData(guid);
            state = playersStore.get(guid).getState();
        }

        Log.outDebug(LogFilter.Lfg, "GetState: [{0}] = {1}", guid, state);

        return state;
    }

    public final LfgState getOldState(ObjectGuid guid) {
        LfgState state;

        if (guid.isParty()) {
            state = groupsStore.get(guid).getOldState();
        } else {
            addPlayerData(guid);
            state = playersStore.get(guid).getOldState();
        }

        Log.outDebug(LogFilter.Lfg, "GetOldState: [{0}] = {1}", guid, state);

        return state;
    }

    public final boolean isVoteKickActive(ObjectGuid gguid) {
        var active = groupsStore.get(gguid).isVoteKickActive();
        Log.outInfo(LogFilter.Lfg, "Group: {0}, Active: {1}", gguid.toString(), active);

        return active;
    }


    public final int getDungeon(ObjectGuid guid) {
        return getDungeon(guid, true);
    }

    public final int getDungeon(ObjectGuid guid, boolean asId) {
        if (!groupsStore.containsKey(guid)) {
            return 0;
        }

        var dungeon = groupsStore.get(guid).getDungeon(asId);
        Log.outDebug(LogFilter.Lfg, "GetDungeon: [{0}] asId: {1} = {2}", guid, asId, dungeon);

        return dungeon;
    }

    public final int getDungeonMapId(ObjectGuid guid) {
        if (!groupsStore.containsKey(guid)) {
            return 0;
        }

        var dungeonId = groupsStore.get(guid).getDungeon(true);
        int mapId = 0;

        if (dungeonId != 0) {
            var dungeon = getLFGDungeon(dungeonId);

            if (dungeon != null) {
                mapId = dungeon.map;
            }
        }

        Log.outError(LogFilter.Lfg, "GetDungeonMapId: [{0}] = {1} (DungeonId = {2})", guid, mapId, dungeonId);

        return mapId;
    }

    public final LfgRoles getRoles(ObjectGuid guid) {
        var roles = playersStore.get(guid).getRoles();
        Log.outDebug(LogFilter.Lfg, "GetRoles: [{0}] = {1}", guid, roles);

        return roles;
    }

    public final ArrayList<Integer> getSelectedDungeons(ObjectGuid guid) {
        Log.outDebug(LogFilter.Lfg, "GetSelectedDungeons: [{0}]", guid);

        return playersStore.get(guid).getSelectedDungeons();
    }

    public final int getSelectedRandomDungeon(ObjectGuid guid) {
        if (getState(guid) != LfgState.NONE) {
            var dungeons = getSelectedDungeons(guid);

            if (!dungeons.isEmpty()) {
                var dungeon = getLFGDungeon(dungeons.get(0));

                if (dungeon != null && dungeon.type == LfgType.raid) {
                    return dungeons.get(0);
                }
            }
        }

        return 0;
    }

    public final HashMap<Integer, LfgLockInfoData> getLockedDungeons(ObjectGuid guid) {
        HashMap<Integer, LfgLockInfoData> lockDic = new HashMap<Integer, LfgLockInfoData>();
        var player = global.getObjAccessor().findConnectedPlayer(guid);

        if (!player) {
            Log.outWarn(LogFilter.Lfg, "{0} not ingame while retrieving his LockedDungeons.", guid.toString());

            return lockDic;
        }

        var level = player.getLevel();
        var expansion = player.getSession().getExpansion();
        var dungeons = getDungeonsByRandom(0);
        var denyJoin = !player.getSession().hasPermission(RBACPermissions.JoinDungeonFinder);

        for (var it : dungeons) {
            var dungeon = getLFGDungeon(it);

            if (dungeon == null) // should never happen - We provide a list from sLFGDungeonStore
            {
                continue;
            }

            LfgLockStatusType lockStatus = LfgLockStatusType.forValue(0);
            AccessRequirement ar;

            if (denyJoin) {
                lockStatus = LfgLockStatusType.RaidLocked;
            } else if (dungeon.expansion > (int) expansion.getValue()) {
                lockStatus = LfgLockStatusType.InsufficientExpansion;
            } else if (global.getDisableMgr().isDisabledFor(DisableType.Map, dungeon.map, player)) {
                lockStatus = LfgLockStatusType.NotInSeason;
            } else if (global.getDisableMgr().isDisabledFor(DisableType.LFGMap, dungeon.map, player)) {
                lockStatus = LfgLockStatusType.RaidLocked;
            } else if (dungeon.difficulty.getValue() > Difficulty.NORMAL.getValue() && global.getInstanceLockMgr().findActiveInstanceLock(guid, new MapDb2Entries(dungeon.map, dungeon.difficulty)) != null) {
                lockStatus = LfgLockStatusType.RaidLocked;
            } else if (dungeon.seasonal && !isSeasonActive(dungeon.id)) {
                lockStatus = LfgLockStatusType.NotInSeason;
            } else if (dungeon.requiredItemLevel > player.getAverageItemLevel()) {
                lockStatus = LfgLockStatusType.TooLowGearScore;
            } else if ((ar = global.getObjectMgr().getAccessRequirement(dungeon.map, dungeon.difficulty)) != null) {
                if (ar.getAchievement() != 0 && !player.hasAchieved(ar.getAchievement())) {
                    lockStatus = LfgLockStatusType.MissingAchievement;
                } else if (player.getTeam() == Team.ALLIANCE && ar.getQuestA() != 0 && !player.getQuestRewardStatus(ar.getQuestA())) {
                    lockStatus = LfgLockStatusType.QuestNotCompleted;
                } else if (player.getTeam() == Team.Horde && ar.getQuestH() != 0 && !player.getQuestRewardStatus(ar.getQuestH())) {
                    lockStatus = LfgLockStatusType.QuestNotCompleted;
                } else if (ar.getItem() != 0) {
                    if (!player.hasItemCount(ar.getItem()) && (ar.getItem2() == 0 || !player.hasItemCount(ar.getItem2()))) {
                        lockStatus = LfgLockStatusType.MissingItem;
                    }
                } else if (ar.getItem2() != 0 && !player.hasItemCount(ar.getItem2())) {
                    lockStatus = LfgLockStatusType.MissingItem;
                }
            } else {
                var levels = global.getDB2Mgr().GetContentTuningData(dungeon.contentTuningId, player.getPlayerData().ctrOptions.getValue().contentTuningConditionMask);

                if (levels != null) {
                    if (levels.getValue().minLevel > level) {
                        lockStatus = LfgLockStatusType.TooLowLevel;
                    }

                    if (levels.getValue().maxLevel < level) {
                        lockStatus = LfgLockStatusType.TooHighLevel;
                    }
                }
            }

			/* @todo VoA closed if WG is not under team control (LFG_LOCKSTATUS_RAID_LOCKED)
			lockData = LFG_LOCKSTATUS_TOO_HIGH_GEAR_SCORE;
			lockData = LFG_LOCKSTATUS_ATTUNEMENT_TOO_LOW_LEVEL;
			lockData = LFG_LOCKSTATUS_ATTUNEMENT_TOO_HIGH_LEVEL;
			*/
            if (lockStatus != 0) {
                lockDic.put(dungeon.entry(), new LfgLockInfoData(lockStatus, dungeon.requiredItemLevel, player.getAverageItemLevel()));
            }
        }

        return lockDic;
    }

    public final byte getKicksLeft(ObjectGuid guid) {
        var kicks = groupsStore.get(guid).getKicksLeft();
        Log.outDebug(LogFilter.Lfg, "GetKicksLeft: [{0}] = {1}", guid, kicks);

        return kicks;
    }

    public final void setState(ObjectGuid guid, LfgState state) {
        if (guid.isParty()) {
            if (!groupsStore.containsKey(guid)) {
                groupsStore.put(guid, new LFGGroupData());
            }

            var data = groupsStore.get(guid);
            data.setState(state);
        } else {
            var data = playersStore.get(guid);
            data.setState(state);
        }
    }

    public final void setSelectedDungeons(ObjectGuid guid, ArrayList<Integer> dungeons) {
        addPlayerData(guid);
        Log.outDebug(LogFilter.Lfg, "SetSelectedDungeons: [{0}] Dungeons: {1}", guid, concatenateDungeons(dungeons));
        playersStore.get(guid).setSelectedDungeons(dungeons);
    }

    public final void removeGroupData(ObjectGuid guid) {
        Log.outDebug(LogFilter.Lfg, "RemoveGroupData: [{0}]", guid);
        var it = groupsStore.get(guid);

        if (it == null) {
            return;
        }

        var state = getState(guid);
        // If group is being formed after proposal success do nothing more
        var players = it.getPlayers();

        for (var _guid : players) {
            setGroup(guid, ObjectGuid.Empty);

            if (state != LfgState.Proposal) {
                setState(guid, LfgState.NONE);
                sendLfgUpdateStatus(guid, new LfgUpdateData(LfgUpdateType.RemovedFromQueue), true);
            }
        }

        groupsStore.remove(guid);
    }

    public final byte removePlayerFromGroup(ObjectGuid gguid, ObjectGuid guid) {
        return groupsStore.get(gguid).removePlayer(guid);
    }

    public final void addPlayerToGroup(ObjectGuid gguid, ObjectGuid guid) {
        if (!groupsStore.containsKey(gguid)) {
            groupsStore.put(gguid, new LFGGroupData());
        }

        groupsStore.get(gguid).addPlayer(guid);
    }

    public final void setLeader(ObjectGuid gguid, ObjectGuid leader) {
        if (!groupsStore.containsKey(gguid)) {
            groupsStore.put(gguid, new LFGGroupData());
        }

        groupsStore.get(gguid).setLeader(leader);
    }

    public final void setTeam(ObjectGuid guid, Team team) {
        if (WorldConfig.getBoolValue(WorldCfg.AllowTwoSideInteractionGroup)) {
            team = Team.forValue(0);
        }

        playersStore.get(guid).setTeam(team);
    }

    public final ObjectGuid getGroup(ObjectGuid guid) {
        addPlayerData(guid);

        return playersStore.get(guid).getGroup();
    }

    public final void setGroup(ObjectGuid guid, ObjectGuid group) {
        addPlayerData(guid);
        playersStore.get(guid).setGroup(group);
    }

    public final byte getPlayerCount(ObjectGuid guid) {
        return groupsStore.get(guid).getPlayerCount();
    }

    public final ObjectGuid getLeader(ObjectGuid guid) {
        return groupsStore.get(guid).getLeader();
    }

    public final boolean hasIgnore(ObjectGuid guid1, ObjectGuid guid2) {
        var plr1 = global.getObjAccessor().findPlayer(guid1);
        var plr2 = global.getObjAccessor().findPlayer(guid2);

        return plr1 != null && plr2 != null && (plr1.getSocial().hasIgnore(guid2, plr2.getSession().getAccountGUID()) || plr2.getSocial().hasIgnore(guid1, plr1.getSession().getAccountGUID()));
    }

    public final void sendLfgRoleChosen(ObjectGuid guid, ObjectGuid pguid, LfgRoles roles) {
        var player = global.getObjAccessor().findPlayer(guid);

        if (player) {
            player.getSession().sendLfgRoleChosen(pguid, roles);
        }
    }

    public final void sendLfgRoleCheckUpdate(ObjectGuid guid, LfgRoleCheck roleCheck) {
        var player = global.getObjAccessor().findPlayer(guid);

        if (player) {
            player.getSession().sendLfgRoleCheckUpdate(roleCheck);
        }
    }

    public final void sendLfgUpdateStatus(ObjectGuid guid, LfgUpdateData data, boolean party) {
        var player = global.getObjAccessor().findPlayer(guid);

        if (player) {
            player.getSession().sendLfgUpdateStatus(data, party);
        }
    }

    public final void sendLfgJoinResult(ObjectGuid guid, LfgJoinResultData data) {
        var player = global.getObjAccessor().findPlayer(guid);

        if (player) {
            player.getSession().sendLfgJoinResult(data);
        }
    }

    public final void sendLfgBootProposalUpdate(ObjectGuid guid, LfgPlayerBoot boot) {
        var player = global.getObjAccessor().findPlayer(guid);

        if (player) {
            player.getSession().sendLfgBootProposalUpdate(boot);
        }
    }

    public final void sendLfgUpdateProposal(ObjectGuid guid, LfgProposal proposal) {
        var player = global.getObjAccessor().findPlayer(guid);

        if (player) {
            player.getSession().sendLfgProposalUpdate(proposal);
        }
    }

    public final void sendLfgQueueStatus(ObjectGuid guid, LfgQueueStatusData data) {
        var player = global.getObjAccessor().findPlayer(guid);

        if (player) {
            player.getSession().sendLfgQueueStatus(data);
        }
    }

    public final boolean isLfgGroup(ObjectGuid guid) {
        return !guid.isEmpty() && guid.isParty() && groupsStore.get(guid).isLfgGroup();
    }

    public final byte getQueueId(ObjectGuid guid) {
        if (guid.isParty()) {
            var players = getPlayers(guid);
            var pguid = players.isEmpty() ? ObjectGuid.Empty : players.get(0);

            if (!pguid.isEmpty()) {
                return (byte) getTeam(pguid).getValue();
            }
        }

        return (byte) getTeam(guid).getValue();
    }

    public final LFGQueue getQueue(ObjectGuid guid) {
        var queueId = getQueueId(guid);

        if (!queuesStore.containsKey(queueId)) {
            queuesStore.put(queueId, new LFGQueue());
        }

        return queuesStore.get(queueId);
    }

    public final boolean allQueued(ArrayList<ObjectGuid> check) {
        if (check.isEmpty()) {
            return false;
        }

        for (var guid : check) {
            var state = getState(guid);

            if (state != LfgState.queued) {
                if (state != LfgState.Proposal) {
                    Log.outDebug(LogFilter.Lfg, "Unexpected state found while trying to form new group. Guid: {0}, State: {1}", guid.toString(), state);
                }

                return false;
            }
        }

        return true;
    }

    public final long getQueueJoinTime(ObjectGuid guid) {
        var queueId = getQueueId(guid);
        var lfgQueue = queuesStore.get(queueId);

        if (lfgQueue != null) {
            return lfgQueue.getJoinTime(guid);
        }

        return 0;
    }

    // Only for debugging purposes
    public final void clean() {
        queuesStore.clear();
    }

    public final boolean isOptionEnabled(LfgOptions option) {
        return m_options.hasFlag(option);
    }

    public final LfgOptions getOptions() {
        return m_options;
    }

    public final void setOptions(LfgOptions options) {
        m_options = options;
    }

    public final LfgUpdateData getLfgStatus(ObjectGuid guid) {
        var playerData = playersStore.get(guid);

        return new LfgUpdateData(LfgUpdateType.UpdateStatus, playerData.getState(), playerData.getSelectedDungeons());
    }

    public final String dumpQueueInfo(boolean full) {
        var size = (int) queuesStore.size();

        var str = "Number of Queues: " + size + "\n";

        for (var pair : queuesStore.entrySet()) {
            var queued = pair.getValue().dumpQueueInfo();
            var compatibles = pair.getValue().dumpCompatibleInfo(full);
            str += queued + compatibles;
        }

        return str;
    }

    public final void setupGroupMember(ObjectGuid guid, ObjectGuid gguid) {
        ArrayList<Integer> dungeons = new ArrayList<>();
        dungeons.add(getDungeon(gguid));
        setSelectedDungeons(guid, dungeons);
        setState(guid, getState(gguid));
        setGroup(guid, gguid);
        addPlayerToGroup(gguid, guid);
    }

    public final boolean selectedRandomLfgDungeon(ObjectGuid guid) {
        if (getState(guid) != LfgState.NONE) {
            var dungeons = getSelectedDungeons(guid);

            if (!dungeons.isEmpty()) {
                var dungeon = getLFGDungeon(dungeons.get(0));

                if (dungeon != null && (dungeon.type == LfgType.random || dungeon.seasonal)) {
                    return true;
                }
            }
        }

        return false;
    }

    public final boolean inLfgDungeonMap(ObjectGuid guid, int map, Difficulty difficulty) {
        if (!guid.isParty()) {
            guid = getGroup(guid);
        }

        var dungeonId = getDungeon(guid, true);

        if (dungeonId != 0) {
            var dungeon = getLFGDungeon(dungeonId);

            if (dungeon != null) {
                if (dungeon.map == map && dungeon.difficulty == difficulty) {
                    return true;
                }
            }
        }

        return false;
    }

    public final int getLFGDungeonEntry(int id) {
        if (id != 0) {
            var dungeon = getLFGDungeon(id);

            if (dungeon != null) {
                return dungeon.entry();
            }
        }

        return 0;
    }

    public final ArrayList<Integer> getRandomAndSeasonalDungeons(int level, int expansion, int contentTuningReplacementConditionMask) {
        ArrayList<Integer> randomDungeons = new ArrayList<>();

        for (var dungeon : lfgDungeonStore.values()) {
            if (!(dungeon.type == LfgType.random || (dungeon.seasonal && global.getLFGMgr().isSeasonActive(dungeon.id)))) {
                continue;
            }

            if (dungeon.expansion > expansion) {
                continue;
            }

            var levels = global.getDB2Mgr().GetContentTuningData(dungeon.contentTuningId, contentTuningReplacementConditionMask);

            if (levels != null) {
                if (levels.getValue().minLevel > level || level > levels.getValue().maxLevel) {
                    continue;
                }
            }

            randomDungeons.add(dungeon.entry());
        }

        return randomDungeons;
    }

    private void _SaveToDB(ObjectGuid guid, int db_guid) {
        if (!guid.isParty()) {
            return;
        }

        SQLTransaction trans = new SQLTransaction();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_LFG_DATA);
        stmt.AddValue(0, db_guid);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.INS_LFG_DATA);
        stmt.AddValue(0, db_guid);
        stmt.AddValue(1, getDungeon(guid));
        stmt.AddValue(2, (int) getState(guid).getValue());
        trans.append(stmt);

        DB.characters.CommitTransaction(trans);
    }

    private LFGDungeonData getLFGDungeon(int id) {
        return lfgDungeonStore.get(id);
    }

    private void getCompatibleDungeons(ArrayList<Integer> dungeons, ArrayList<ObjectGuid> players, HashMap<ObjectGuid, HashMap<Integer, LfgLockInfoData>> lockMap, ArrayList<String> playersMissingRequirement, boolean isContinue) {
        lockMap.clear();
        HashMap<Integer, Integer> lockedDungeons = new HashMap<Integer, Integer>();
        ArrayList<Integer> dungeonsToRemove = new ArrayList<>();

        for (var guid : players) {
            if (dungeons.isEmpty()) {
                break;
            }

            var cachedLockMap = getLockedDungeons(guid);
            var player = global.getObjAccessor().findConnectedPlayer(guid);

            for (var it2 : cachedLockMap.entrySet()) {
                if (dungeons.isEmpty()) {
                    break;
                }

                var dungeonId = (it2.getKey() & 0x00FFFFFF); // Compare dungeon ids

                if (dungeons.contains(dungeonId)) {
                    var eraseDungeon = true;

                    // Don't remove the dungeon if team members are trying to continue a locked instance
                    if (it2.getValue().lockStatus == LfgLockStatusType.RaidLocked && isContinue) {
                        var dungeon = getLFGDungeon(dungeonId);
                        MapDb2Entries entries = new MapDb2Entries(dungeon.map, dungeon.difficulty);
                        var playerBind = global.getInstanceLockMgr().findActiveInstanceLock(guid, entries);

                        if (playerBind != null) {
                            var dungeonInstanceId = playerBind.getInstanceId();

                            TValue lockedDungeon;
                            if (!(lockedDungeons.containsKey(dungeonId) && (lockedDungeon = lockedDungeons.get(dungeonId)) == lockedDungeon) || lockedDungeon == dungeonInstanceId) {
                                eraseDungeon = false;
                            }

                            lockedDungeons.put(dungeonId, dungeonInstanceId);
                        }
                    }

                    if (eraseDungeon) {
                        dungeonsToRemove.add(dungeonId);
                    }

                    if (!lockMap.containsKey(guid)) {
                        lockMap.put(guid, new HashMap<Integer, LfgLockInfoData>());
                    }

                    lockMap.get(guid).put(it2.getKey(), it2.getValue());
                    playersMissingRequirement.add(player.getName());
                }
            }
        }

        for (var dungeonIdToRemove : dungeonsToRemove) {
            dungeons.remove((Integer) dungeonIdToRemove);
        }

        if (!dungeons.isEmpty()) {
            lockMap.clear();
        }
    }

    private void makeNewGroup(LfgProposal proposal) {
        ArrayList<ObjectGuid> players = new ArrayList<>();
        ArrayList<ObjectGuid> tankPlayers = new ArrayList<>();
        ArrayList<ObjectGuid> healPlayers = new ArrayList<>();
        ArrayList<ObjectGuid> dpsPlayers = new ArrayList<>();
        ArrayList<ObjectGuid> playersToTeleport = new ArrayList<>();

        for (var it : proposal.players.entrySet()) {
            var guid = it.getKey();

            if (guid == proposal.leader) {
                players.add(guid);
            } else {
                switch (it.getValue().role & ~LfgRoles.leader) {
                    case LfgRoles.Tank:
                        tankPlayers.add(guid);

                        break;
                    case LfgRoles.Healer:
                        healPlayers.add(guid);

                        break;
                    case LfgRoles.Damage:
                        dpsPlayers.add(guid);

                        break;
                }
            }

            if (proposal.isNew || ObjectGuid.opNotEquals(getGroup(guid), proposal.group)) {
                playersToTeleport.add(guid);
            }
        }

        players.addAll(tankPlayers);
        players.addAll(healPlayers);
        players.addAll(dpsPlayers);

        // Set the dungeon difficulty
        var dungeon = getLFGDungeon(proposal.dungeonId);

        var grp = !proposal.group.isEmpty() ? global.getGroupMgr().getGroupByGUID(proposal.group) : null;

        for (var pguid : players) {
            var player = global.getObjAccessor().findConnectedPlayer(pguid);

            if (!player) {
                continue;
            }

            var group = player.getGroup();

            if (group && group != grp) {
                group.removeMember(player.getGUID());
            }

            if (!grp) {
                grp = new PlayerGroup();
                grp.convertToLFG();
                grp.create(player);
                var gguid = grp.GUID;
                setState(gguid, LfgState.Proposal);
                global.getGroupMgr().addGroup(grp);
            } else if (group != grp) {
                grp.addMember(player);
            }

            grp.setLfgRoles(pguid, proposal.players.get(pguid).role);

            // Add the cooldown spell if queued for a random dungeon
            var dungeons = getSelectedDungeons(player.getGUID());

            if (!dungeons.isEmpty()) {
                var rDungeonId = dungeons.get(0);
                var rDungeon = getLFGDungeon(rDungeonId);

                if (rDungeon != null && rDungeon.type == LfgType.random) {
                    player.castSpell(player, SharedConst.LFGSpellDungeonCooldown, false);
                }
            }
        }

        grp.setDungeonDifficultyID(dungeon.difficulty);
        var guid = grp.GUID;
        setDungeon(guid, dungeon.entry());
        setState(guid, LfgState.Dungeon);

        _SaveToDB(guid, grp.DbStoreId);

        // Teleport Player
        for (var it : playersToTeleport) {
            var player = global.getObjAccessor().findPlayer(it);

            if (player) {
                teleportPlayer(player, false);
            }
        }

        // Update group info
        grp.sendUpdate();
    }

    private void removeProposal(java.util.Map.entry<Integer, LfgProposal> itProposal, LfgUpdateType type) {
        var proposal = itProposal.getValue();
        proposal.state = LfgProposalState.Failed;

        Log.outDebug(LogFilter.Lfg, "RemoveProposal: Proposal {0}, state FAILED, UpdateType {1}", itProposal.getKey(), type);

        // Mark all people that didn't answered as no accept
        if (type == LfgUpdateType.ProposalFailed) {
            for (var it : proposal.players) {
                if (it.value.accept == LfgAnswer.Pending) {
                    it.value.accept = LfgAnswer.Deny;
                }
            }
        }

        // Mark players/groups to be removed
        ArrayList<ObjectGuid> toRemove = new ArrayList<>();

        for (var it : proposal.players) {
            if (it.value.accept == LfgAnswer.Agree) {
                continue;
            }

            var guid = !it.value.group.IsEmpty ? it.value.group : it.key;

            // Player didn't accept or still pending when no secs left
            if (it.value.accept == LfgAnswer.Deny || type == LfgUpdateType.ProposalFailed) {
                it.value.accept = LfgAnswer.Deny;
                toRemove.add(guid);
            }
        }

        // Notify players
        for (var it : proposal.players) {
            var guid = it.key;
            var gguid = !it.value.group.IsEmpty ? it.value.group : guid;

            sendLfgUpdateProposal(guid, proposal);

            if (toRemove.contains(gguid)) // Didn't accept or in same group that someone that didn't accept
            {
                LfgUpdateData updateData = new LfgUpdateData();

                if (it.value.accept == LfgAnswer.Deny) {
                    updateData.updateType = type;
                    Log.outDebug(LogFilter.Lfg, "RemoveProposal: [{0}] didn't accept. Removing from queue and compatible cache", guid);
                } else {
                    updateData.updateType = LfgUpdateType.RemovedFromQueue;
                    Log.outDebug(LogFilter.Lfg, "RemoveProposal: [{0}] in same group that someone that didn't accept. Removing from queue and compatible cache", guid);
                }

                restoreState(guid, "Proposal Fail (didn't accepted or in group with someone that didn't accept");

                if (gguid != guid) {
                    restoreState(it.value.group, "Proposal Fail (someone in group didn't accepted)");
                    sendLfgUpdateStatus(guid, updateData, true);
                } else {
                    sendLfgUpdateStatus(guid, updateData, false);
                }
            } else {
                Log.outDebug(LogFilter.Lfg, "RemoveProposal: Readding [{0}] to queue.", guid);
                setState(guid, LfgState.queued);

                if (gguid != guid) {
                    setState(gguid, LfgState.queued);
                    sendLfgUpdateStatus(guid, new LfgUpdateData(LfgUpdateType.AddedToQueue, getSelectedDungeons(guid)), true);
                } else {
                    sendLfgUpdateStatus(guid, new LfgUpdateData(LfgUpdateType.AddedToQueue, getSelectedDungeons(guid)), false);
                }
            }
        }

        var queue = getQueue(proposal.players.first().key);

        // Remove players/groups from queue
        for (var guid : toRemove) {
            queue.removeFromQueue(guid);
            proposal.queues.remove(guid);
        }

        // Readd to queue
        for (var guid : proposal.queues) {
            queue.addToQueue(guid, true);
        }

        proposalsStore.remove(itProposal.getKey());
    }

    private ArrayList<Integer> getDungeonsByRandom(int randomdungeon) {
        var dungeon = getLFGDungeon(randomdungeon);
        var group = (byte) (dungeon != null ? dungeon.group : 0);

        return cachedDungeonMapStore.get(group);
    }

    private void restoreState(ObjectGuid guid, String debugMsg) {
        if (guid.isParty()) {
            var data = groupsStore.get(guid);
            data.restoreState();
        } else {
            var data = playersStore.get(guid);
            data.restoreState();
        }
    }

    private void setVoteKick(ObjectGuid gguid, boolean active) {
        var data = groupsStore.get(gguid);
        Log.outInfo(LogFilter.Lfg, "Group: {0}, New state: {1}, Previous: {2}", gguid.toString(), active, data.isVoteKickActive());

        data.setVoteKick(active);
    }

    private void setDungeon(ObjectGuid guid, int dungeon) {
        addPlayerData(guid);
        Log.outDebug(LogFilter.Lfg, "SetDungeon: [{0}] dungeon {1}", guid, dungeon);
        groupsStore.get(guid).setDungeon(dungeon);
    }

    private void setRoles(ObjectGuid guid, LfgRoles roles) {
        addPlayerData(guid);
        Log.outDebug(LogFilter.Lfg, "SetRoles: [{0}] roles: {1}", guid, roles);
        playersStore.get(guid).setRoles(roles);
    }

    private void decreaseKicksLeft(ObjectGuid guid) {
        Log.outDebug(LogFilter.Lfg, "DecreaseKicksLeft: [{0}]", guid);
        groupsStore.get(guid).decreaseKicksLeft();
    }

    private void addPlayerData(ObjectGuid guid) {
        if (playersStore.containsKey(guid)) {
            return;
        }

        playersStore.put(guid, new LFGPlayerData());
    }

    private void setTicket(ObjectGuid guid, RideTicket ticket) {
        playersStore.get(guid).setTicket(ticket);
    }

    private void removePlayerData(ObjectGuid guid) {
        Log.outDebug(LogFilter.Lfg, "RemovePlayerData: [{0}]", guid);
        playersStore.remove(guid);
    }

    private Team getTeam(ObjectGuid guid) {
        return playersStore.get(guid).getTeam();
    }

    private LfgRoles filterClassRoles(Player player, LfgRoles roles) {
        var allowedRoles = (int) LfgRoles.leader.getValue();

        for (int i = 0; i < MAX_SPECIALIZATIONS; ++i) {
            var specialization = global.getDB2Mgr().GetChrSpecializationByIndex(player.getClass(), i);

            if (specialization != null) {
                allowedRoles |= (1 << (specialization.role + 1));
            }
        }

        return roles.getValue() & LfgRoles.forValue(allowedRoles).getValue();
    }

    private ArrayList<ObjectGuid> getPlayers(ObjectGuid guid) {
        return groupsStore.get(guid).getPlayers();
    }

    private boolean isSeasonActive(int dungeonId) {
        switch (dungeonId) {
            case 285: // The Headless Horseman
                return global.getGameEventMgr().isHolidayActive(HolidayIds.HallowsEnd);
            case 286: // The Frost Lord Ahune
                return global.getGameEventMgr().isHolidayActive(HolidayIds.MidsummerFireFestival);
            case 287: // Coren Direbrew
                return global.getGameEventMgr().isHolidayActive(HolidayIds.Brewfest);
            case 288: // The Crown Chemical Co.
                return global.getGameEventMgr().isHolidayActive(HolidayIds.LoveIsInTheAir);
            case 744: // Random Timewalking Dungeon (Burning Crusade)
                return global.getGameEventMgr().isHolidayActive(HolidayIds.TimewalkingDungeonEventBcDefault);
            case 995: // Random Timewalking Dungeon (Wrath of the Lich King)
                return global.getGameEventMgr().isHolidayActive(HolidayIds.TimewalkingDungeonEventLkDefault);
            case 1146: // Random Timewalking Dungeon (Cataclysm)
                return global.getGameEventMgr().isHolidayActive(HolidayIds.TimewalkingDungeonEventCataDefault);
            case 1453: // Timewalker MoP
                return global.getGameEventMgr().isHolidayActive(HolidayIds.TimewalkingDungeonEventMopDefault);
        }

        return false;
    }
}
