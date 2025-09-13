package com.github.azeroth.game.battleground;


import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.group.PlayerGroup;
import game.WorldConfig;

import java.util.ArrayList;
import java.util.HashMap;


public class BattlegroundQueue {
    private final HashMap<ObjectGuid, PlayerQueueInfo> m_QueuedPlayers = new HashMap<ObjectGuid, PlayerQueueInfo>();

    /**
     * This two dimensional array is used to store All queued groups
     * First dimension specifies the bgTypeId
     * Second dimension specifies the player's group types -
     * BG_QUEUE_PREMADE_ALLIANCE  is used for premade alliance groups and alliance rated arena teams
     * BG_QUEUE_PREMADE_HORDE     is used for premade horde groups and horde rated arena teams
     * BattlegroundConst.BgQueueNormalAlliance   is used for normal (or small) alliance groups or non-rated arena matches
     * BattlegroundConst.BgQueueNormalHorde      is used for normal (or small) horde groups or non-rated arena matches
     */
    private final ArrayList<GroupQueueInfo>[][] m_QueuedGroups = new ArrayList<GroupQueueInfo>[BattlegroundBracketId.max.getValue()][];

    private final int[][][] m_WaitTimes = new int[SharedConst.PvpTeamsCount][][];
    private final int[][] m_WaitTimeLastPlayer = new int[SharedConst.PvpTeamsCount][];
    private final int[][] m_SumOfWaitTimes = new int[SharedConst.PvpTeamsCount][];

    // Event handler
    private final eventSystem m_events = new eventSystem();
    private final SelectionPool[] m_SelectionPools = new SelectionPool[SharedConst.PvpTeamsCount];

    private final BattlegroundQueueTypeId m_queueId;

    public BattlegroundQueue(BattlegroundQueueTypeId queueId) {
        m_queueId = queueId;

        for (var i = 0; i < BattlegroundBracketId.max.getValue(); ++i) {
            m_QueuedGroups[i] = new ArrayList<GroupQueueInfo>[BattlegroundConst.BgQueueTypesCount];

            for (var c = 0; c < BattlegroundConst.BgQueueTypesCount; ++c) {
                m_QueuedGroups[i][c] = new ArrayList<>();
            }
        }

        for (var i = 0; i < SharedConst.PvpTeamsCount; ++i) {
            m_WaitTimes[i] = new int[BattlegroundBracketId.max.getValue()][];

            for (var c = 0; c < BattlegroundBracketId.max.getValue(); ++c) {
                m_WaitTimes[i][c] = new int[SharedConst.CountOfPlayersToAverageWaitTime];
            }

            m_WaitTimeLastPlayer[i] = new int[BattlegroundBracketId.max.getValue()];
            m_SumOfWaitTimes[i] = new int[BattlegroundBracketId.max.getValue()];
        }

        m_SelectionPools[0] = new SelectionPool();
        m_SelectionPools[1] = new SelectionPool();
    }

    // add group or player (grp == null) to bg queue with the given leader and bg specifications

    public final GroupQueueInfo addGroup(Player leader, PlayerGroup group, Team team, PvpDifficultyRecord bracketEntry, boolean isPremade, int ArenaRating, int MatchmakerRating) {
        return addGroup(leader, group, team, bracketEntry, isPremade, ArenaRating, MatchmakerRating, 0);
    }

    public final GroupQueueInfo addGroup(Player leader, PlayerGroup group, Team team, PvpDifficultyRecord bracketEntry, boolean isPremade, int ArenaRating, int MatchmakerRating, int arenateamid) {
        var bracketId = bracketEntry.getBracketId();

        // create new ginfo
        GroupQueueInfo ginfo = new GroupQueueInfo();
        ginfo.arenaTeamId = arenateamid;
        ginfo.isInvitedToBGInstanceGUID = 0;
        ginfo.joinTime = gameTime.GetGameTimeMS();
        ginfo.removeInviteTime = 0;
        ginfo.team = team;
        ginfo.arenaTeamRating = ArenaRating;
        ginfo.arenaMatchmakerRating = MatchmakerRating;
        ginfo.opponentsTeamRating = 0;
        ginfo.opponentsMatchmakerRating = 0;

        ginfo.players.clear();

        //compute index (if group is premade or joined a rated match) to queues
        int index = 0;

        if (!m_queueId.rated && !isPremade) {
            index += SharedConst.PvpTeamsCount;
        }

        if (ginfo.team == Team.Horde) {
            index++;
        }

        Log.outDebug(LogFilter.Battleground, "Adding Group to BattlegroundQueue bgTypeId : {0}, bracket_id : {1}, index : {2}", m_queueId.battlemasterListId, bracketId, index);

        var lastOnlineTime = gameTime.GetGameTimeMS();

        //announce world (this don't need mutex)
        if (m_queueId.rated && WorldConfig.getBoolValue(WorldCfg.ArenaQueueAnnouncerEnable)) {
            var arenaTeam = global.getArenaTeamMgr().getArenaTeamById(arenateamid);

            if (arenaTeam != null) {
                global.getWorldMgr().sendWorldText(SysMessage.ArenaQueueAnnounceWorldJoin, arenaTeam.getName(), m_queueId.teamSize, m_queueId.teamSize, ginfo.arenaTeamRating);
            }
        }

        //add players from group to ginfo
        if (group) {
            for (var refe = group.getFirstMember(); refe != null; refe = refe.next()) {
                var member = refe.getSource();

                if (!member) {
                    continue; // this should never happen
                }

                PlayerQueueInfo pl_info = new PlayerQueueInfo();
                pl_info.lastOnlineTime = lastOnlineTime;
                pl_info.groupInfo = ginfo;

                m_QueuedPlayers.put(member.getGUID(), pl_info);
                // add the pinfo to ginfo's list
                ginfo.players.put(member.getGUID(), pl_info);
            }
        } else {
            PlayerQueueInfo pl_info = new PlayerQueueInfo();
            pl_info.lastOnlineTime = lastOnlineTime;
            pl_info.groupInfo = ginfo;

            m_QueuedPlayers.put(leader.getGUID(), pl_info);
            ginfo.players.put(leader.getGUID(), pl_info);
        }

        {
            //add GroupInfo to m_QueuedGroups
            //ACE_Guard<ACE_Recursive_Thread_Mutex> guard(m_Lock);
            m_QueuedGroups[bracketId.getValue()][index].add(ginfo);

            //announce to world, this code needs mutex
            if (!m_queueId.rated && !isPremade && WorldConfig.getBoolValue(WorldCfg.BattlegroundQueueAnnouncerEnable)) {
                var bg = global.getBattlegroundMgr().getBattlegroundTemplate(BattlegroundTypeId.forValue(m_queueId.battlemasterListId));

                if (bg) {
                    var bgName = bg.getName();
                    var MinPlayers = bg.getMinPlayersPerTeam();
                    int qHorde = 0;
                    int qAlliance = 0;
                    int q_min_level = bracketEntry.minLevel;
                    int q_max_level = bracketEntry.maxLevel;

                    for (var groupQueueInfo : m_QueuedGroups[bracketId.getValue()][BattlegroundConst.BgQueueNormalAlliance]) {
                        if (groupQueueInfo.isInvitedToBGInstanceGUID == 0) {
                            qAlliance += (int) groupQueueInfo.players.size();
                        }
                    }

                    for (var groupQueueInfo : m_QueuedGroups[bracketId.getValue()][BattlegroundConst.BgQueueNormalHorde]) {
                        if (groupQueueInfo.isInvitedToBGInstanceGUID == 0) {
                            qHorde += (int) groupQueueInfo.players.size();
                        }
                    }

                    // Show queue status to player only (when joining queue)
                    if (WorldConfig.getBoolValue(WorldCfg.BattlegroundQueueAnnouncerPlayeronly)) {
                        leader.sendSysMessage(SysMessage.BgQueueAnnounceSelf, bgName, q_min_level, q_max_level, qAlliance, (MinPlayers > qAlliance) ? MinPlayers - qAlliance : 0, qHorde, (MinPlayers > qHorde) ? MinPlayers - qHorde : 0);
                    }
                    // System message
                    else {
                        global.getWorldMgr().sendWorldText(SysMessage.BgQueueAnnounceWorld, bgName, q_min_level, q_max_level, qAlliance, (MinPlayers > qAlliance) ? MinPlayers - qAlliance : 0, qHorde, (MinPlayers > qHorde) ? MinPlayers - qHorde : 0);
                    }
                }
            }
            //release mutex
        }

        return ginfo;
    }

    public final int getAverageQueueWaitTime(GroupQueueInfo ginfo, BattlegroundBracketId bracket_id) {
        int team_index = TeamId.ALLIANCE; //default set to TeamIndex.Alliance - or non rated arenas!

        if (m_queueId.teamSize == 0) {
            if (ginfo.team == Team.Horde) {
                team_index = TeamId.HORDE;
            }
        } else {
            if (m_queueId.rated) {
                team_index = TeamId.HORDE; //for rated arenas use TeamIndex.Horde
            }
        }

        //check if there is enought values(we always add values > 0)
        if (m_WaitTimes[team_index][bracket_id.getValue()][SharedConst.CountOfPlayersToAverageWaitTime - 1] != 0) {
            return (m_SumOfWaitTimes[team_index][bracket_id.getValue()] / SharedConst.CountOfPlayersToAverageWaitTime);
        } else {
            //if there aren't enough values return 0 - not available
            return 0;
        }
    }

    //remove player from queue and from group info, if group info is empty then remove it too
    public final void removePlayer(ObjectGuid guid, boolean decreaseInvitedCount) {
        var bracket_id = -1; // signed for proper for-loop finish

        //remove player from map, if he's there
        var playerQueueInfo = m_QueuedPlayers.get(guid);

        if (playerQueueInfo == null) {
            var playerName = "Unknown";
            var player = global.getObjAccessor().findPlayer(guid);

            if (player) {
                playerName = player.getName();
            }

            Log.outDebug(LogFilter.Battleground, "BattlegroundQueue: couldn't find player {0} ({1})", playerName, guid.toString());

            return;
        }

        var group = playerQueueInfo.groupInfo;
        GroupQueueInfo groupQueseInfo = null;
        // mostly people with the highest levels are in Battlegrounds, thats why
        // we count from MAX_Battleground_QUEUES - 1 to 0

        var index = (group.team == Team.Horde) ? BattlegroundConst.BgQueuePremadeHorde : BattlegroundConst.BgQueuePremadeAlliance;

        for (var bracket_id_tmp = BattlegroundBracketId.max.getValue() - 1; bracket_id_tmp >= 0 && bracket_id == -1; --bracket_id_tmp) {
            //we must check premade and normal team's queue - because when players from premade are joining bg,
            //they leave groupinfo so we can't use its players size to find out index
            for (var j = index; j < BattlegroundConst.BgQueueTypesCount; j += SharedConst.PvpTeamsCount) {
                for (var k : m_QueuedGroups[bracket_id_tmp][j]) {
                    if (k == group) {
                        bracket_id = bracket_id_tmp;
                        groupQueseInfo = k;
                        //we must store index to be able to erase iterator
                        index = j;

                        break;
                    }
                }
            }
        }

        //player can't be in queue without group, but just in case
        if (bracket_id == -1) {
            Log.outError(LogFilter.Battleground, "BattlegroundQueue: ERROR Cannot find groupinfo for {0}", guid.toString());

            return;
        }

        Log.outDebug(LogFilter.Battleground, "BattlegroundQueue: Removing {0}, from bracket_id {1}", guid.toString(), bracket_id);

        // ALL variables are correctly set
        // We can ignore leveling up in queue - it should not cause crash
        // remove player from group
        // if only one player there, remove group

        // remove player queue info from group queue info
        if (group.players.ContainsKey(guid)) {
            group.players.remove(guid);
        }

        // if invited to bg, and should decrease invited count, then do it
        if (decreaseInvitedCount && group.isInvitedToBGInstanceGUID != 0) {
            var bg = global.getBattlegroundMgr().getBattleground(group.isInvitedToBGInstanceGUID, BattlegroundTypeId.forValue(m_queueId.battlemasterListId));

            if (bg) {
                bg.decreaseInvitedCount(group.team);
            }
        }

        // remove player queue info
        m_QueuedPlayers.remove(guid);

        // announce to world if arena team left queue for rated match, show only once
        if (m_queueId.teamSize != 0 && m_queueId.rated && group.players.isEmpty() && WorldConfig.getBoolValue(WorldCfg.ArenaQueueAnnouncerEnable)) {
            var team = global.getArenaTeamMgr().getArenaTeamById(group.arenaTeamId);

            if (team != null) {
                global.getWorldMgr().sendWorldText(SysMessage.ArenaQueueAnnounceWorldExit, team.getName(), m_queueId.teamSize, m_queueId.teamSize, group.arenaTeamRating);
            }
        }

        // if player leaves queue and he is invited to rated arena match, then he have to lose
        if (group.isInvitedToBGInstanceGUID != 0 && m_queueId.rated && decreaseInvitedCount) {
            var at = global.getArenaTeamMgr().getArenaTeamById(group.arenaTeamId);

            if (at != null) {
                Log.outDebug(LogFilter.Battleground, "UPDATING memberLost's personal arena rating for {0} by opponents rating: {1}", guid.toString(), group.opponentsTeamRating);
                var player = global.getObjAccessor().findPlayer(guid);

                if (player) {
                    at.memberLost(player, group.opponentsMatchmakerRating);
                } else {
                    at.offlineMemberLost(guid, group.opponentsMatchmakerRating);
                }

                at.saveToDB();
            }
        }

        // remove group queue info if needed
        if (group.players.isEmpty()) {
            m_QueuedGroups[bracket_id][index].remove(groupQueseInfo);

            return;
        }

        // if group wasn't empty, so it wasn't deleted, and player have left a rated
        // queue . everyone from the group should leave too
        // don't remove recursively if already invited to bg!
        if (group.isInvitedToBGInstanceGUID == 0 && m_queueId.rated) {
            // remove next player, this is recursive
            // first send removal information
            var plr2 = global.getObjAccessor().findConnectedPlayer(group.players.FirstOrDefault().key);

            if (plr2) {
                var queueSlot = plr2.getBattlegroundQueueIndex(m_queueId);

                plr2.removeBattlegroundQueueId(m_queueId); // must be called this way, because if you move this call to
                // queue.removeplayer, it causes bugs

                com.github.azeroth.game.networking.packet.BattlefieldStatusNone battlefieldStatus;
                tangible.OutObject<com.github.azeroth.game.networking.packet.BattlefieldStatusNone> tempOut_battlefieldStatus = new tangible.OutObject<com.github.azeroth.game.networking.packet.BattlefieldStatusNone>();
                global.getBattlegroundMgr().buildBattlegroundStatusNone(tempOut_battlefieldStatus, plr2, queueSlot, plr2.getBattlegroundQueueJoinTime(m_queueId));
                battlefieldStatus = tempOut_battlefieldStatus.outArgValue;
                plr2.sendPacket(battlefieldStatus);
            }

            // then actually delete, this may delete the group as well!
            removePlayer(group.players.first().key, decreaseInvitedCount);
        }
    }

    //returns true when player pl_guid is in queue and is invited to bgInstanceGuid
    public final boolean isPlayerInvited(ObjectGuid pl_guid, int bgInstanceGuid, int removeTime) {
        var queueInfo = m_QueuedPlayers.get(pl_guid);

        return (queueInfo != null && queueInfo.groupInfo.isInvitedToBGInstanceGUID == bgInstanceGuid && queueInfo.groupInfo.removeInviteTime == removeTime);
    }

    public final boolean getPlayerGroupInfoData(ObjectGuid guid, tangible.OutObject<GroupQueueInfo> ginfo) {
        ginfo.outArgValue = null;
        var playerQueueInfo = m_QueuedPlayers.get(guid);

        if (playerQueueInfo == null) {
            return false;
        }

        ginfo.outArgValue = playerQueueInfo.groupInfo;

        return true;
    }

    public final void updateEvents(int diff) {
        m_events.update(diff);
    }

    /**
     * this method is called when group is inserted, or player / group is removed from BG Queue - there is only one player's status changed, so we don't use while (true) cycles to invite whole queue
     * it must be called after fully adding the members of a group to ensure group joining
     * should be called from Battleground.RemovePlayer function in some cases
     *
     * @param diff
     * @param bgTypeId
     * @param bracket_id
     * @param arenaType
     * @param isRated
     * @param arenaRating
     */
    public final void battlegroundQueueUpdate(int diff, BattlegroundBracketId bracket_id, int arenaRating) {
        //if no players in queue - do nothing
        if (m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueuePremadeAlliance].isEmpty() && m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueuePremadeHorde].isEmpty() && m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalAlliance].isEmpty() && m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalHorde].isEmpty()) {
            return;
        }

        // Battleground with free slot for player should be always in the beggining of the queue
        // maybe it would be better to create bgfreeslotqueue for each bracket_id
        var bgQueues = global.getBattlegroundMgr().getBGFreeSlotQueueStore(m_queueId);

        for (var bg : bgQueues) {
            // DO NOT allow queue manager to invite new player to rated games
            if (!bg.isRated() && bg.getBracketId() == bracket_id && bg.getStatus() > BattlegroundStatus.WaitQueue.getValue() && bg.getStatus() < BattlegroundStatus.WaitLeave.getValue()) {
                // clear selection pools
                m_SelectionPools[TeamId.ALLIANCE].init();
                m_SelectionPools[TeamId.HORDE].init();

                // call a function that does the job for us
                fillPlayersToBG(bg, bracket_id);

                // now everything is set, invite players
                for (var queueInfo : m_SelectionPools[TeamId.ALLIANCE].selectedGroups) {
                    inviteGroupToBG(queueInfo, bg, queueInfo.team);
                }

                for (var queueInfo : m_SelectionPools[TeamId.HORDE].selectedGroups) {
                    inviteGroupToBG(queueInfo, bg, queueInfo.team);
                }

                if (!bg.hasFreeSlots()) {
                    bg.removeFromBGFreeSlotQueue();
                }
            }
        }

        // finished iterating through the bgs with free slots, maybe we need to create a new bg

        var bg_template = global.getBattlegroundMgr().getBattlegroundTemplate(BattlegroundTypeId.forValue(m_queueId.battlemasterListId));

        if (!bg_template) {
            Log.outError(LogFilter.Battleground, String.format("Battleground: Update: bg template not found for %1$s", m_queueId.battlemasterListId));

            return;
        }

        var bracketEntry = global.getDB2Mgr().GetBattlegroundBracketById(bg_template.getMapId(), bracket_id);

        if (bracketEntry == null) {
            Log.outError(LogFilter.Battleground, "Battleground: Update: bg bracket entry not found for map {0} bracket id {1}", bg_template.getMapId(), bracket_id);

            return;
        }

        // get the min. players per team, properly for larger arenas as well. (must have full teams for arena matches!)
        var MinPlayersPerTeam = bg_template.getMinPlayersPerTeam();
        var MaxPlayersPerTeam = bg_template.getMaxPlayersPerTeam();

        if (bg_template.isArena()) {
            MaxPlayersPerTeam = m_queueId.teamSize;
            MinPlayersPerTeam = global.getBattlegroundMgr().isArenaTesting() ? 1 : m_queueId.teamSize;
        } else if (global.getBattlegroundMgr().isTesting()) {
            MinPlayersPerTeam = 1;
        }

        m_SelectionPools[TeamId.ALLIANCE].init();
        m_SelectionPools[TeamId.HORDE].init();

        if (bg_template.isBattleground()) {
            if (checkPremadeMatch(bracket_id, MinPlayersPerTeam, MaxPlayersPerTeam)) {
                // create new Battleground
                var bg2 = global.getBattlegroundMgr().createNewBattleground(m_queueId, bracketEntry);

                if (bg2 == null) {
                    Log.outError(LogFilter.Battleground, String.format("BattlegroundQueue.Update - Cannot create Battleground: %1$s", m_queueId.battlemasterListId));

                    return;
                }

                // invite those selection pools
                for (int i = 0; i < SharedConst.PvpTeamsCount; i++) {
                    for (var queueInfo : m_SelectionPools[TeamId.ALLIANCE + i].selectedGroups) {
                        inviteGroupToBG(queueInfo, bg2, queueInfo.team);
                    }
                }

                bg2.startBattleground();
                //clear structures
                m_SelectionPools[TeamId.ALLIANCE].init();
                m_SelectionPools[TeamId.HORDE].init();
            }
        }

        // now check if there are in queues enough players to start new game of (normal Battleground, or non-rated arena)
        if (!m_queueId.rated) {
            // if there are enough players in pools, start new Battleground or non rated arena
            if (checkNormalMatch(bg_template, bracket_id, MinPlayersPerTeam, MaxPlayersPerTeam) || (bg_template.isArena() && checkSkirmishForSameFaction(bracket_id, MinPlayersPerTeam))) {
                // we successfully created a pool
                var bg2 = global.getBattlegroundMgr().createNewBattleground(m_queueId, bracketEntry);

                if (bg2 == null) {
                    Log.outError(LogFilter.Battleground, String.format("BattlegroundQueue.Update - Cannot create Battleground: %1$s", m_queueId.battlemasterListId));

                    return;
                }

                // invite those selection pools
                for (int i = 0; i < SharedConst.PvpTeamsCount; i++) {
                    for (var queueInfo : m_SelectionPools[TeamId.ALLIANCE + i].selectedGroups) {
                        inviteGroupToBG(queueInfo, bg2, queueInfo.team);
                    }
                }

                // start bg
                bg2.startBattleground();
            }
        } else if (bg_template.isArena()) {
            // found out the minimum and maximum ratings the newly added team should battle against
            // arenaRating is the rating of the latest joined team, or 0
            // 0 is on (automatic update call) and we must set it to team's with longest wait time
            if (arenaRating == 0) {
                GroupQueueInfo front1 = null;
                GroupQueueInfo front2 = null;

                if (!m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueuePremadeAlliance].isEmpty()) {
                    front1 = m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueuePremadeAlliance].get(0);
                    arenaRating = front1.arenaMatchmakerRating;
                }

                if (!m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueuePremadeHorde].isEmpty()) {
                    front2 = m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueuePremadeHorde].get(0);
                    arenaRating = front2.arenaMatchmakerRating;
                }

                if (front1 != null && front2 != null) {
                    if (front1.joinTime < front2.joinTime) {
                        arenaRating = front1.arenaMatchmakerRating;
                    }
                } else if (front1 == null && front2 == null) {
                    return; //queues are empty
                }
            }

            //set rating range
            var arenaMinRating = (arenaRating <= global.getBattlegroundMgr().getMaxRatingDifference()) ? 0 : arenaRating - global.getBattlegroundMgr().getMaxRatingDifference();
            var arenaMaxRating = arenaRating + global.getBattlegroundMgr().getMaxRatingDifference();
            // if max rating difference is set and the time past since server startup is greater than the rating discard time
            // (after what time the ratings aren't taken into account when making teams) then
            // the discard time is current_time - time_to_discard, teams that joined after that, will have their ratings taken into account
            // else leave the discard time on 0, this way all ratings will be discarded
            var discardTime = (int) (gameTime.GetGameTimeMS() - global.getBattlegroundMgr().getRatingDiscardTimer());

            // we need to find 2 teams which will play next game
            var queueArray = new GroupQueueInfo[SharedConst.PvpTeamsCount];
            byte found = 0;
            byte team = 0;

            for (var i = (byte) BattlegroundConst.BgQueuePremadeAlliance; i < BattlegroundConst.BgQueueNormalAlliance; i++) {
                // take the group that joined first
                for (var queueInfo : m_QueuedGroups[bracket_id.getValue()][i]) {
                    // if group match conditions, then add it to pool
                    if (queueInfo.isInvitedToBGInstanceGUID == 0 && ((queueInfo.arenaMatchmakerRating >= arenaMinRating && queueInfo.arenaMatchmakerRating <= arenaMaxRating) || queueInfo.joinTime < discardTime)) {
                        queueArray[found++] = queueInfo;
                        team = i;

                        break;
                    }
                }
            }

            if (found == 0) {
                return;
            }

            if (found == 1) {
                for (var queueInfo : m_QueuedGroups[bracket_id.getValue()][team]) {
                    if (queueInfo.isInvitedToBGInstanceGUID == 0 && ((queueInfo.arenaMatchmakerRating >= arenaMinRating && queueInfo.arenaMatchmakerRating <= arenaMaxRating) || queueInfo.joinTime < discardTime) && queueArray[0].arenaTeamId != queueInfo.arenaTeamId) {
                        queueArray[found++] = queueInfo;

                        break;
                    }
                }
            }

            //if we have 2 teams, then start new arena and invite players!
            if (found == 2) {
                var aTeam = queueArray[TeamId.ALLIANCE];
                var hTeam = queueArray[TeamId.HORDE];
                var arena = global.getBattlegroundMgr().createNewBattleground(m_queueId, bracketEntry);

                if (!arena) {
                    Log.outError(LogFilter.Battleground, "BattlegroundQueue.Update couldn't create arena instance for rated arena match!");

                    return;
                }

                aTeam.opponentsTeamRating = hTeam.arenaTeamRating;
                hTeam.opponentsTeamRating = aTeam.arenaTeamRating;
                aTeam.opponentsMatchmakerRating = hTeam.arenaMatchmakerRating;
                hTeam.opponentsMatchmakerRating = aTeam.arenaMatchmakerRating;
                Log.outDebug(LogFilter.Battleground, "setting oposite teamrating for team {0} to {1}", aTeam.arenaTeamId, aTeam.opponentsTeamRating);
                Log.outDebug(LogFilter.Battleground, "setting oposite teamrating for team {0} to {1}", hTeam.arenaTeamId, hTeam.opponentsTeamRating);

                // now we must move team if we changed its faction to another faction queue, because then we will spam log by errors in Queue.RemovePlayer
                if (aTeam.team != Team.ALLIANCE) {
                    m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueuePremadeAlliance].add(0, aTeam);
                    m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueuePremadeHorde].remove(queueArray[TeamId.ALLIANCE]);
                }

                if (hTeam.team != Team.Horde) {
                    m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueuePremadeHorde].add(0, hTeam);
                    m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueuePremadeAlliance].remove(queueArray[TeamId.HORDE]);
                }

                arena.setArenaMatchmakerRating(Team.ALLIANCE, aTeam.arenaMatchmakerRating);
                arena.setArenaMatchmakerRating(Team.Horde, hTeam.arenaMatchmakerRating);
                inviteGroupToBG(aTeam, arena, Team.ALLIANCE);
                inviteGroupToBG(hTeam, arena, Team.Horde);

                Log.outDebug(LogFilter.Battleground, "Starting rated arena match!");
                arena.startBattleground();
            }
        }
    }

    public final BattlegroundQueueTypeId getQueueId() {
        return m_queueId;
    }

    private void playerInvitedToBGUpdateAverageWaitTime(GroupQueueInfo ginfo, BattlegroundBracketId bracket_id) {
        var timeInQueue = time.GetMSTimeDiff(ginfo.joinTime, gameTime.GetGameTimeMS());
        int team_index = TeamId.ALLIANCE; //default set to TeamIndex.Alliance - or non rated arenas!

        if (m_queueId.teamSize == 0) {
            if (ginfo.team == Team.Horde) {
                team_index = TeamId.HORDE;
            }
        } else {
            if (m_queueId.rated) {
                team_index = TeamId.HORDE; //for rated arenas use TeamIndex.Horde
            }
        }

        //store pointer to arrayindex of player that was added first
        var lastPlayerAddedPointer = m_WaitTimeLastPlayer[team_index][bracket_id.getValue()];
        //remove his time from sum
        m_SumOfWaitTimes[team_index][bracket_id.getValue()] -= m_WaitTimes[team_index][bracket_id.getValue()][lastPlayerAddedPointer];
        //set average time to new
        m_WaitTimes[team_index][bracket_id.getValue()][lastPlayerAddedPointer] = timeInQueue;
        //add new time to sum
        m_SumOfWaitTimes[team_index][bracket_id.getValue()] += timeInQueue;
        //set index of last player added to next one
        lastPlayerAddedPointer++;
        m_WaitTimeLastPlayer[team_index][bracket_id.getValue()] = lastPlayerAddedPointer % SharedConst.CountOfPlayersToAverageWaitTime;
    }

    private int getPlayersInQueue(int id) {
        return m_SelectionPools[id].getPlayerCount();
    }

    private boolean inviteGroupToBG(GroupQueueInfo ginfo, Battleground bg, Team side) {
        // set side if needed
        if (side != 0) {
            ginfo.team = side;
        }

        if (ginfo.isInvitedToBGInstanceGUID == 0) {
            // not yet invited
            // set invitation
            ginfo.isInvitedToBGInstanceGUID = bg.getInstanceID();
            var bgTypeId = bg.getTypeID();
            var bgQueueTypeId = bg.getQueueId();
            var bracket_id = bg.getBracketId();

            // set ArenaTeamId for rated matches
            if (bg.isArena() && bg.isRated()) {
                bg.setArenaTeamIdForTeam(ginfo.team, ginfo.arenaTeamId);
            }

            ginfo.removeInviteTime = gameTime.GetGameTimeMS() + BattlegroundConst.InviteAcceptWaitTime;

            // loop through the players
            for (var guid : ginfo.players.keySet()) {
                // get the player
                var player = global.getObjAccessor().findPlayer(guid);

                // if offline, skip him, this should not happen - player is removed from queue when he logs out
                if (!player) {
                    continue;
                }

                // invite the player
                playerInvitedToBGUpdateAverageWaitTime(ginfo, bracket_id);

                // set invited player counters
                bg.increaseInvitedCount(ginfo.team);

                player.setInviteForBattlegroundQueueType(bgQueueTypeId, ginfo.isInvitedToBGInstanceGUID);

                // create remind invite events
                BGQueueInviteEvent inviteEvent = new BGQueueInviteEvent(player.getGUID(), ginfo.isInvitedToBGInstanceGUID, bgTypeId, ArenaTypes.forValue(m_queueId.teamSize), ginfo.removeInviteTime);
                m_events.addEvent(inviteEvent, m_events.CalculateTime(duration.ofSeconds(BattlegroundConst.InvitationRemindTime)), true);
                // create automatic remove events
                BGQueueRemoveEvent removeEvent = new BGQueueRemoveEvent(player.getGUID(), ginfo.isInvitedToBGInstanceGUID, bgQueueTypeId, ginfo.removeInviteTime);
                m_events.addEvent(removeEvent, m_events.CalculateTime(duration.ofSeconds(BattlegroundConst.InviteAcceptWaitTime)), true);

                var queueSlot = player.getBattlegroundQueueIndex(bgQueueTypeId);

                Log.outDebug(LogFilter.Battleground, "Battleground: invited player {0} ({1}) to BG instance {2} queueindex {3} bgtype {4}", player.getName(), player.getGUID().toString(), bg.getInstanceID(), queueSlot, bg.getTypeID());

                com.github.azeroth.game.networking.packet.BattlefieldStatusNeedConfirmation battlefieldStatus;
                tangible.OutObject<com.github.azeroth.game.networking.packet.BattlefieldStatusNeedConfirmation> tempOut_battlefieldStatus = new tangible.OutObject<com.github.azeroth.game.networking.packet.BattlefieldStatusNeedConfirmation>();
                global.getBattlegroundMgr().buildBattlegroundStatusNeedConfirmation(tempOut_battlefieldStatus, bg, player, queueSlot, player.getBattlegroundQueueJoinTime(bgQueueTypeId), BattlegroundConst.InviteAcceptWaitTime, ArenaTypes.forValue(m_queueId.teamSize));
                battlefieldStatus = tempOut_battlefieldStatus.outArgValue;
                player.sendPacket(battlefieldStatus);
            }

            return true;
        }

        return false;
    }

    /*
    This function is inviting players to already running Battlegrounds
    Invitation type is based on config file
    large groups are disadvantageous, because they will be kicked first if invitation type = 1
    */
    private void fillPlayersToBG(Battleground bg, BattlegroundBracketId bracket_id) {
        var hordeFree = bg.getFreeSlotsForTeam(Team.Horde);
        var aliFree = bg.getFreeSlotsForTeam(Team.ALLIANCE);
        var aliCount = m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalAlliance].size();
        var hordeCount = m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalHorde].size();

        // try to get even teams
        if (WorldConfig.getIntValue(WorldCfg.BattlegroundInvitationType) == BattlegroundQueueInvitationType.Even.getValue()) {
            // check if the teams are even
            if (hordeFree == 1 && aliFree == 1) {
                // if we are here, the teams have the same amount of players
                // then we have to allow to join the same amount of players
                var hordeExtra = hordeCount - aliCount;
                var aliExtra = aliCount - hordeCount;

                hordeExtra = Math.max(hordeExtra, 0);
                aliExtra = Math.max(aliExtra, 0);

                if (aliCount != hordeCount) {
                    aliFree -= (int) aliExtra;
                    hordeFree -= (int) hordeExtra;

                    aliFree = Math.max(aliFree, 0);
                    hordeFree = Math.max(hordeFree, 0);
                }
            }
        }

        //count of groups in queue - used to stop cycles
        var alyIndex = 0;

        {
            var listIndex = 0;
            var info = m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalAlliance].FirstOrDefault();

            for (; alyIndex < aliCount && m_SelectionPools[TeamId.ALLIANCE].addGroup(info, aliFree); alyIndex++) {
                info = m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalAlliance].get(listIndex++);
            }
        }

        //the same thing for horde
        var hordeIndex = 0;

        {
            var listIndex = 0;
            var info = m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalHorde].FirstOrDefault();

            for (; hordeIndex < hordeCount && m_SelectionPools[TeamId.HORDE].addGroup(info, hordeFree); hordeIndex++) {
                info = m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalHorde].get(listIndex++);
            }
        }

        //if ofc like BG queue invitation is set in config, then we are happy
        if (WorldConfig.getIntValue(WorldCfg.BattlegroundInvitationType) == BattlegroundQueueInvitationType.NoBalance.getValue()) {
            return;
        }
		/*
		if we reached this code, then we have to solve NP - complete problem called Subset sum problem
		So one solution is to check all possible invitation subgroups, or we can use these conditions:
		1. Last time when BattlegroundQueue.Update was executed we invited all possible players - so there is only small possibility
			that we will invite now whole queue, because only 1 change has been made to queues from the last BattlegroundQueue.Update call
		2. Other thing we should consider is group order in queue
		*/

        // At first we need to compare free space in bg and our selection pool
        var diffAli = (int) (aliFree - m_SelectionPools[TeamId.ALLIANCE].getPlayerCount());
        var diffHorde = (int) (hordeFree - m_SelectionPools[TeamId.HORDE].getPlayerCount());

        while (Math.abs(diffAli - diffHorde) > 1 && (m_SelectionPools[TeamId.HORDE].getPlayerCount() > 0 || m_SelectionPools[TeamId.ALLIANCE].getPlayerCount() > 0)) {
            //each cycle execution we need to kick at least 1 group
            if (diffAli < diffHorde) {
                //kick alliance group, add to pool new group if needed
                if (m_SelectionPools[TeamId.ALLIANCE].kickGroup((int) (diffHorde - diffAli))) {
                    for (; alyIndex < aliCount && m_SelectionPools[TeamId.ALLIANCE].addGroup(m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalAlliance].get(alyIndex), (int) ((aliFree >= diffHorde) ? aliFree - diffHorde : 0)); alyIndex++) {
                        ++alyIndex;
                    }
                }

                //if ali selection is already empty, then kick horde group, but if there are less horde than ali in bg - break;
                if (m_SelectionPools[TeamId.ALLIANCE].getPlayerCount() == 0) {
                    if (aliFree <= diffHorde + 1) {
                        break;
                    }

                    m_SelectionPools[TeamId.HORDE].kickGroup((int) (diffHorde - diffAli));
                }
            } else {
                //kick horde group, add to pool new group if needed
                if (m_SelectionPools[TeamId.HORDE].kickGroup((int) (diffAli - diffHorde))) {
                    for (; hordeIndex < hordeCount && m_SelectionPools[TeamId.HORDE].addGroup(m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalHorde].get(hordeIndex), (int) ((hordeFree >= diffAli) ? hordeFree - diffAli : 0)); hordeIndex++) {
                        ++hordeIndex;
                    }
                }

                if (m_SelectionPools[TeamId.HORDE].getPlayerCount() == 0) {
                    if (hordeFree <= diffAli + 1) {
                        break;
                    }

                    m_SelectionPools[TeamId.ALLIANCE].kickGroup((int) (diffAli - diffHorde));
                }
            }

            //count diffs after small update
            diffAli = (int) (aliFree - m_SelectionPools[TeamId.ALLIANCE].getPlayerCount());
            diffHorde = (int) (hordeFree - m_SelectionPools[TeamId.HORDE].getPlayerCount());
        }
    }

    // this method checks if premade versus premade Battleground is possible
    // then after 30 mins (default) in queue it moves premade group to normal queue
    // it tries to invite as much players as it can - to MaxPlayersPerTeam, because premade groups have more than MinPlayersPerTeam players
    private boolean checkPremadeMatch(BattlegroundBracketId bracket_id, int MinPlayersPerTeam, int MaxPlayersPerTeam) {
        //check match
        if (!m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueuePremadeAlliance].isEmpty() && !m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueuePremadeHorde].isEmpty()) {
            //start premade match
            //if groups aren't invited
            GroupQueueInfo ali_group = null;
            GroupQueueInfo horde_group = null;

            for (var groupQueueInfo : m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueuePremadeAlliance]) {
                ali_group = groupQueueInfo;

                if (ali_group.isInvitedToBGInstanceGUID == 0) {
                    break;
                }
            }

            for (var groupQueueInfo : m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueuePremadeHorde]) {
                horde_group = groupQueueInfo;

                if (horde_group.isInvitedToBGInstanceGUID == 0) {
                    break;
                }
            }

            if (ali_group != null && horde_group != null) {
                m_SelectionPools[TeamId.ALLIANCE].addGroup(ali_group, MaxPlayersPerTeam);
                m_SelectionPools[TeamId.HORDE].addGroup(horde_group, MaxPlayersPerTeam);
                //add groups/players from normal queue to size of bigger group
                var maxPlayers = Math.min(m_SelectionPools[TeamId.ALLIANCE].getPlayerCount(), m_SelectionPools[TeamId.HORDE].getPlayerCount());

                for (int i = 0; i < SharedConst.PvpTeamsCount; i++) {
                    for (var groupQueueInfo : m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalAlliance + i]) {
                        //if groupQueueInfo can join BG and player count is less that maxPlayers, then add group to selectionpool
                        if (groupQueueInfo.isInvitedToBGInstanceGUID == 0 && !m_SelectionPools[i].addGroup(groupQueueInfo, maxPlayers)) {
                            break;
                        }
                    }
                }

                //premade selection pools are set
                return true;
            }
        }

        // now check if we can move group from Premade queue to normal queue (timer has expired) or group size lowered!!
        // this could be 2 cycles but i'm checking only first team in queue - it can cause problem -
        // if first is invited to BG and seconds timer expired, but we can ignore it, because players have only 80 seconds to click to enter bg
        // and when they click or after 80 seconds the queue info is removed from queue
        var time_before = (int) (gameTime.GetGameTimeMS() - WorldConfig.getIntValue(WorldCfg.BattlegroundPremadeGroupWaitForMatch));

        for (int i = 0; i < SharedConst.PvpTeamsCount; i++) {
            if (!m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueuePremadeAlliance + i].isEmpty()) {
                var groupQueueInfo = m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueuePremadeAlliance + i].get(0);

                if (groupQueueInfo.isInvitedToBGInstanceGUID == 0 && (groupQueueInfo.joinTime < time_before || groupQueueInfo.players.count < MinPlayersPerTeam)) {
                    //we must insert group to normal queue and erase pointer from premade queue
                    m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalAlliance + i].add(0, groupQueueInfo);
                    m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueuePremadeAlliance + i].remove(groupQueueInfo);
                }
            }
        }

        //selection pools are not set
        return false;
    }

    // this method tries to create Battleground or arena with MinPlayersPerTeam against MinPlayersPerTeam
    private boolean checkNormalMatch(Battleground bg_template, BattlegroundBracketId bracket_id, int minPlayers, int maxPlayers) {
        var teamIndex = new int[SharedConst.PvpTeamsCount];

        for (int i = 0; i < SharedConst.PvpTeamsCount; i++) {
            teamIndex[i] = 0;

            for (; teamIndex[i] != m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalAlliance + i].size(); ++teamIndex[i]) {
                var groupQueueInfo = m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalAlliance + i].get(teamIndex[i]);

                if (groupQueueInfo.isInvitedToBGInstanceGUID == 0) {
                    m_SelectionPools[i].addGroup(groupQueueInfo, maxPlayers);

                    if (m_SelectionPools[i].getPlayerCount() >= minPlayers) {
                        break;
                    }
                }
            }
        }

        //try to invite same number of players - this cycle may cause longer wait time even if there are enough players in queue, but we want ballanced bg
        int j = TeamId.ALLIANCE;

        if (m_SelectionPools[TeamId.HORDE].getPlayerCount() < m_SelectionPools[TeamId.ALLIANCE].getPlayerCount()) {
            j = TeamId.HORDE;
        }

        if (WorldConfig.getIntValue(WorldCfg.BattlegroundInvitationType) != BattlegroundQueueInvitationType.NoBalance.getValue() && m_SelectionPools[TeamId.HORDE].getPlayerCount() >= minPlayers && m_SelectionPools[TeamId.ALLIANCE].getPlayerCount() >= minPlayers) {
            //we will try to invite more groups to team with less players indexed by j
            ++(teamIndex[j]); //this will not cause a crash, because for cycle above reached break;

            for (; teamIndex[j] != m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalAlliance + j].size(); ++teamIndex[j]) {
                var groupQueueInfo = m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalAlliance + j].get(teamIndex[j]);

                if (groupQueueInfo.isInvitedToBGInstanceGUID == 0) {
                    if (!m_SelectionPools[j].addGroup(groupQueueInfo, m_SelectionPools[(j + 1) % SharedConst.PvpTeamsCount].getPlayerCount())) {
                        break;
                    }
                }
            }

            // do not allow to start bg with more than 2 players more on 1 faction
            if (Math.abs((m_SelectionPools[TeamId.HORDE].getPlayerCount() - m_SelectionPools[TeamId.ALLIANCE].getPlayerCount())) > 2) {
                return false;
            }
        }

        //allow 1v0 if debug bg
        if (global.getBattlegroundMgr().isTesting() && (m_SelectionPools[TeamId.ALLIANCE].getPlayerCount() != 0 || m_SelectionPools[TeamId.HORDE].getPlayerCount() != 0)) {
            return true;
        }

        //return true if there are enough players in selection pools - enable to work .debug bg command correctly
        return m_SelectionPools[TeamId.ALLIANCE].getPlayerCount() >= minPlayers && m_SelectionPools[TeamId.HORDE].getPlayerCount() >= minPlayers;
    }

    // this method will check if we can invite players to same faction skirmish match
    private boolean checkSkirmishForSameFaction(BattlegroundBracketId bracket_id, int minPlayersPerTeam) {
        if (m_SelectionPools[TeamId.ALLIANCE].getPlayerCount() < minPlayersPerTeam && m_SelectionPools[TeamId.HORDE].getPlayerCount() < minPlayersPerTeam) {
            return false;
        }

        int teamIndex = TeamId.ALLIANCE;
        int otherTeam = TeamId.HORDE;
        var otherTeamId = Team.Horde;

        if (m_SelectionPools[TeamId.HORDE].getPlayerCount() == minPlayersPerTeam) {
            teamIndex = TeamId.HORDE;
            otherTeam = TeamId.ALLIANCE;
            otherTeamId = Team.ALLIANCE;
        }

        //clear other team's selection
        m_SelectionPools[otherTeam].init();
        //store last ginfo pointer
        var ginfo = m_SelectionPools[teamIndex].selectedGroups.get(m_SelectionPools[teamIndex].selectedGroups.size() - 1);
        //set itr_team to group that was added to selection pool latest
        var team = 0;

        for (var groupQueueInfo : m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalAlliance + teamIndex]) {
            if (ginfo == groupQueueInfo) {
                break;
            }
        }

        if (team == m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalAlliance + teamIndex].size() - 1) {
            return false;
        }

        var team2 = team;
        ++team2;

        //invite players to other selection pool
        for (; team2 != m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalAlliance + teamIndex].size() - 1; ++team2) {
            var groupQueueInfo = m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalAlliance + teamIndex].get(team2);

            //if selection pool is full then break;
            if (groupQueueInfo.isInvitedToBGInstanceGUID == 0 && !m_SelectionPools[otherTeam].addGroup(groupQueueInfo, minPlayersPerTeam)) {
                break;
            }
        }

        if (m_SelectionPools[otherTeam].getPlayerCount() != minPlayersPerTeam) {
            return false;
        }

        //here we have correct 2 selections and we need to change one teams team and move selection pool teams to other team's queue
        for (var groupQueueInfo : m_SelectionPools[otherTeam].selectedGroups) {
            //set correct team
            groupQueueInfo.team = otherTeamId;
            //add team to other queue
            m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalAlliance + otherTeam].add(0, groupQueueInfo);
            //remove team from old queue
            var team3 = team;
            ++team3;

            for (; team3 != m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalAlliance + teamIndex].size() - 1; ++team3) {
                var groupQueueInfo1 = m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalAlliance + teamIndex].get(team3);

                if (groupQueueInfo1 == groupQueueInfo) {
                    m_QueuedGroups[bracket_id.getValue()][BattlegroundConst.BgQueueNormalAlliance + teamIndex].remove(groupQueueInfo1);

                    break;
                }
            }
        }

        return true;
    }

    // class to select and invite groups to bg
    private static class SelectionPool {
        public final ArrayList<GroupQueueInfo> selectedGroups = new ArrayList<>();

        private int playerCount;

        public final void init() {
            selectedGroups.clear();
            playerCount = 0;
        }

        public final boolean addGroup(GroupQueueInfo ginfo, int desiredCount) {
            //if group is larger than desired count - don't allow to add it to pool
            if (ginfo.isInvitedToBGInstanceGUID == 0 && desiredCount >= playerCount + ginfo.players.size()) {
                selectedGroups.add(ginfo);
                // increase selected players count
                playerCount += (int) ginfo.players.size();

                return true;
            }

            if (playerCount < desiredCount) {
                return true;
            }

            return false;
        }

        public final boolean kickGroup(int size) {
            //find maxgroup or LAST group with size == size and kick it
            var found = false;
            GroupQueueInfo groupToKick = null;

            for (var groupQueueInfo : selectedGroups) {
                if (Math.abs(groupQueueInfo.players.size() - size) <= 1) {
                    groupToKick = groupQueueInfo;
                    found = true;
                } else if (!found && groupQueueInfo.players.size() >= groupToKick.players.size()) {
                    groupToKick = groupQueueInfo;
                }
            }

            //if pool is empty, do nothing
            if (getPlayerCount() != 0) {
                //update player count
                var ginfo = groupToKick;
                selectedGroups.remove(groupToKick);
                PlayerCount -= (int) ginfo.players.size();

                //return false if we kicked smaller group or there are enough players in selection pool
                if (ginfo.players.size() <= size + 1) {
                    return false;
                }
            }

            return true;
        }

        public final int getPlayerCount() {
            return playerCount;
        }
    }
}
