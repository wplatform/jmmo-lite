package com.github.azeroth.game.guild;


import com.github.azeroth.game.achievement.CriteriaManager;
import com.github.azeroth.game.achievement.GuildAchievementMgr;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.entity.item.ItemPosCount;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.listener.interfaces.iguild.*;
import game.ObjectManager;
import com.github.azeroth.game.world.WorldSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.locale;


public class Guild {
    private final ArrayList<RankInfo> m_ranks = new ArrayList<>();
    private final HashMap<ObjectGuid, member> m_members = new HashMap<ObjectGuid, member>();
    private final ArrayList<bankTab> m_bankTabs = new ArrayList<>();
    // These are actually ordered lists. The first element is the oldest entry.
    private final LogHolder<EventLogEntry> m_eventLog = new LogHolder<EventLogEntry>();
    private final LogHolder<BankEventLogEntry>[] m_bankEventLog = new LogHolder<BankEventLogEntry>[GuildConst.MaxBankTabs + 1];
    private final LogHolder<NewsLogEntry> m_newsLog = new LogHolder<NewsLogEntry>();
    private final GuildAchievementMgr m_achievementSys;
    private long m_id;
    private String m_name;
    private ObjectGuid m_leaderGuid = ObjectGuid.EMPTY;
    private String m_motd;
    private String m_info;
    private long m_createdDate;
    private emblemInfo m_emblemInfo = new emblemInfo();
    private int m_accountsNumber;
    private long m_bankMoney;

    public guild() {
        m_achievementSys = new GuildAchievementMgr(this);

        for (var i = 0; i < m_bankEventLog.length; ++i) {
            m_bankEventLog[i] = new LogHolder<BankEventLogEntry>();
        }
    }

    public static void sendCommandResult(WorldSession session, GuildCommandType type, GuildCommandError errCode) {
        sendCommandResult(session, type, errCode, "");
    }

    public static void sendCommandResult(WorldSession session, GuildCommandType type, GuildCommandError errCode, String param) {
        GuildCommandResult resultPacket = new GuildCommandResult();
        resultPacket.command = type;
        resultPacket.result = errCode;
        resultPacket.name = param;
        session.sendPacket(resultPacket);
    }

    public static void sendSaveEmblemResult(WorldSession session, GuildEmblemError errCode) {
        PlayerSaveGuildEmblem saveResponse = new PlayerSaveGuildEmblem();
        saveResponse.error = errCode;
        session.sendPacket(saveResponse);
    }

    public final boolean create(Player pLeader, String name) {
        // Check if guild with such name already exists
        if (global.getGuildMgr().getGuildByName(name) != null) {
            return false;
        }

        var pLeaderSession = pLeader.getSession();

        if (pLeaderSession == null) {
            return false;
        }

        m_id = global.getGuildMgr().generateGuildId();
        m_leaderGuid = pLeader.getGUID();
        m_name = name;
        m_info = "";
        m_motd = "No message set.";
        m_bankMoney = 0;
        m_createdDate = GameTime.getGameTime();

        Log.outDebug(LogFilter.guild, "GUILD: creating guild [{0}] for leader {1} ({2})", name, pLeader.getName(), m_leaderGuid);

        SQLTransaction trans = new SQLTransaction();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GUILD_MEMBERS);
        stmt.AddValue(0, m_id);
        trans.append(stmt);

        byte index = 0;
        stmt = DB.characters.GetPreparedStatement(CharStatements.INS_GUILD);
        stmt.AddValue(index, m_id);
        stmt.AddValue(++index, name);
        stmt.AddValue(++index, m_leaderGuid.getCounter());
        stmt.AddValue(++index, m_info);
        stmt.AddValue(++index, m_motd);
        stmt.AddValue(++index, m_createdDate);
        stmt.AddValue(++index, m_emblemInfo.getStyle());
        stmt.AddValue(++index, m_emblemInfo.getColor());
        stmt.AddValue(++index, m_emblemInfo.getBorderStyle());
        stmt.AddValue(++index, m_emblemInfo.getBorderColor());
        stmt.AddValue(++index, m_emblemInfo.getBackgroundColor());
        stmt.AddValue(++index, m_bankMoney);
        trans.append(stmt);

        _CreateDefaultGuildRanks(trans, pLeaderSession.getSessionDbLocaleIndex()); // Create default ranks
        var ret = addMember(trans, m_leaderGuid, GuildRankId.GuildMaster); // Add guildmaster

        DB.characters.CommitTransaction(trans);

        if (ret) {
            var leader = getMember(m_leaderGuid);

            if (leader != null) {
                sendEventNewLeader(leader, null);
            }

            global.getScriptMgr().<IGuildOnCreate>ForEach(p -> p.onCreate(this, pLeader, name));
        }

        return ret;
    }

    public final void disband() {
        global.getScriptMgr().<IGuildOnDisband>ForEach(p -> p.onDisband(this));

        broadcastPacket(new GuildEventDisbanded());

        SQLTransaction trans = new SQLTransaction();

        while (!m_members.isEmpty()) {
            var member = m_members.firstEntry();
            deleteMember(trans, member.value.getGUID(), true);
        }

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GUILD);
        stmt.AddValue(0, m_id);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GUILD_RANKS);
        stmt.AddValue(0, m_id);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GUILD_BANK_TABS);
        stmt.AddValue(0, m_id);
        trans.append(stmt);

        // Free bank tab used memory and delete items stored in them
        _DeleteBankItems(trans, true);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GUILD_BANK_ITEMS);
        stmt.AddValue(0, m_id);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GUILD_BANK_RIGHTS);
        stmt.AddValue(0, m_id);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GUILD_BANK_EVENTLOGS);
        stmt.AddValue(0, m_id);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GUILD_EVENTLOGS);
        stmt.AddValue(0, m_id);
        trans.append(stmt);

        DB.characters.CommitTransaction(trans);

        global.getGuildMgr().removeGuild(m_id);
    }

    public final void saveToDB() {
        SQLTransaction trans = new SQLTransaction();

        getAchievementMgr().saveToDB(trans);

        DB.characters.CommitTransaction(trans);
    }

    public final void updateMemberData(Player player, GuildMemberData dataid, int value) {
        var member = getMember(player.getGUID());

        if (member != null) {
            switch (dataid) {
                case ZoneId:
                    member.setZoneId(value);

                    break;
                case AchievementPoints:
                    member.setAchievementPoints(value);

                    break;
                case Level:
                    member.setLevel(value);

                    break;
                default:
                    Log.outError(LogFilter.guild, "Guild.UpdateMemberData: Called with incorrect DATAID {0} (value {1})", dataid, value);

                    return;
            }
        }
    }

    public final boolean setName(String name) {
        if (Objects.equals(m_name, name) || StringUtil.isEmpty(name) || name.length() > 24 || global.getObjectMgr().isReservedName(name) || !ObjectManager.isValidCharterName(name)) {
            return false;
        }

        m_name = name;
        var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GUILD_NAME);
        stmt.AddValue(0, m_name);
        stmt.AddValue(1, getId());
        DB.characters.execute(stmt);

        GuildNameChanged guildNameChanged = new GuildNameChanged();
        guildNameChanged.guildGUID = getGUID();
        guildNameChanged.guildName = m_name;
        broadcastPacket(guildNameChanged);

        return true;
    }

    public final void handleRoster() {
        handleRoster(null);
    }

    public final void handleRoster(WorldSession session) {
        GuildRoster roster = new GuildRoster();
        roster.numAccounts = (int) m_accountsNumber;
        roster.createDate = (int) m_createdDate;
        roster.guildFlags = 0;

        var sendOfficerNote = _HasRankRight(session.getPlayer(), GuildRankRights.ViewOffNote);

        for (var member : m_members.values()) {
            GuildRosterMemberData memberData = new GuildRosterMemberData();

            memberData.guid = member.getGUID();
            memberData.rankID = (int) member.getRankId();
            memberData.areaID = (int) member.getZoneId();
            memberData.personalAchievementPoints = (int) member.getAchievementPoints();
            memberData.guildReputation = (int) member.getTotalReputation();
            memberData.lastSave = member.getInactiveDays();

            //GuildRosterProfessionData

            memberData.virtualRealmAddress = global.getWorldMgr().getVirtualRealmAddress();
            memberData.status = (byte) member.getFlags();
            memberData.level = member.getLevel();
            memberData.classID = (byte) member.getClass();
            memberData.gender = (byte) member.getGender();
            memberData.raceID = (byte) member.getRace();

            memberData.authenticated = false;
            memberData.sorEligible = false;

            memberData.name = member.getName();
            memberData.note = member.getPublicNote();

            if (sendOfficerNote) {
                memberData.officerNote = member.getOfficerNote();
            }

            roster.memberData.add(memberData);
        }

        roster.welcomeText = m_motd;
        roster.infoText = m_info;

        if (session != null) {
            session.sendPacket(roster);
        }
    }

    public final void sendQueryResponse(WorldSession session) {
        QueryGuildInfoResponse response = new QueryGuildInfoResponse();
        response.guildGUID = getGUID();
        response.hasGuildInfo = true;

        response.info.guildGuid = getGUID();
        response.info.virtualRealmAddress = global.getWorldMgr().getVirtualRealmAddress();

        response.info.emblemStyle = m_emblemInfo.getStyle();
        response.info.emblemColor = m_emblemInfo.getColor();
        response.info.borderStyle = m_emblemInfo.getBorderStyle();
        response.info.borderColor = m_emblemInfo.getBorderColor();
        response.info.backgroundColor = m_emblemInfo.getBackgroundColor();

        for (var rankInfo : m_ranks) {
            response.info.ranks.add(new QueryGuildInfoResponse.guildInfo.RankInfo((byte) rankInfo.getId().getValue(), (byte) rankInfo.getOrder().getValue(), rankInfo.getName()));
        }

        response.info.guildName = m_name;

        session.sendPacket(response);
    }

    public final void sendGuildRankInfo(WorldSession session) {
        GuildRanks ranks = new GuildRanks();

        for (var rankInfo : m_ranks) {
            GuildRankData rankData = new GuildRankData();

            rankData.rankID = (byte) rankInfo.getId().getValue();
            rankData.rankOrder = (byte) rankInfo.getOrder().getValue();
            rankData.flags = (int) rankInfo.getRights().getValue();
            rankData.withdrawGoldLimit = (rankInfo.getId() == GuildRankId.GuildMaster ? Integer.MAX_VALUE : (rankInfo.getBankMoneyPerDay() / MoneyConstants.gold));
            rankData.rankName = rankInfo.getName();

            for (byte j = 0; j < GuildConst.MaxBankTabs; ++j) {
                rankData.TabFlags[j] = (int) rankInfo.getBankTabRights(j).getValue();
                rankData.TabWithdrawItemLimit[j] = (int) rankInfo.getBankTabSlotsPerDay(j);
            }

            ranks.ranks.add(rankData);
        }

        session.sendPacket(ranks);
    }

    public final void handleSetAchievementTracking(WorldSession session, ArrayList<Integer> achievementIds) {
        var player = session.getPlayer();

        var member = getMember(player.getGUID());

        if (member != null) {
            ArrayList<Integer> criteriaIds = new ArrayList<>();

            for (var achievementId : achievementIds) {
                var achievement = CliDB.AchievementStorage.get(achievementId);

                if (achievement != null) {
                    var tree = global.getCriteriaMgr().getCriteriaTree(achievement.CriteriaTree);

                    if (tree != null) {
                        CriteriaManager.walkCriteriaTree(tree, node ->
                        {
                            if (node.criteria != null) {
                                criteriaIds.add(node.criteria.id);
                            }
                        });
                    }
                }
            }

            member.setTrackedCriteriaIds(criteriaIds);
            getAchievementMgr().sendAllTrackedCriterias(player, member.getTrackedCriteriaIds());
        }
    }

    public final void handleGetAchievementMembers(WorldSession session, int achievementId) {
        getAchievementMgr().sendAchievementMembers(session.getPlayer(), achievementId);
    }

    public final void handleSetMOTD(WorldSession session, String motd) {
        if (Objects.equals(m_motd, motd)) {
            return;
        }

        // Player must have rights to set MOTD
        if (!_HasRankRight(session.getPlayer(), GuildRankRights.SetMotd)) {
            sendCommandResult(session, GuildCommandType.EditMOTD, GuildCommandError.Permissions);
        } else {
            m_motd = motd;

            global.getScriptMgr().<IGuildOnMOTDChanged>ForEach(p -> p.OnMOTDChanged(this, motd));

            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GUILD_MOTD);
            stmt.AddValue(0, motd);
            stmt.AddValue(1, m_id);
            DB.characters.execute(stmt);

            sendEventMOTD(session, true);
        }
    }

    public final void handleSetInfo(WorldSession session, String info) {
        if (Objects.equals(m_info, info)) {
            return;
        }

        // Player must have rights to set guild's info
        if (_HasRankRight(session.getPlayer(), GuildRankRights.ModifyGuildInfo)) {
            m_info = info;

            global.getScriptMgr().<IGuildOnInfoChanged>ForEach(p -> p.OnInfoChanged(this, info));

            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GUILD_INFO);
            stmt.AddValue(0, info);
            stmt.AddValue(1, m_id);
            DB.characters.execute(stmt);
        }
    }

    public final void handleSetEmblem(WorldSession session, EmblemInfo emblemInfo) {
        var player = session.getPlayer();

        if (!_IsLeader(player)) {
            sendSaveEmblemResult(session, GuildEmblemError.NotGuildMaster); // "Only guild leaders can create emblems."
        } else if (!player.hasEnoughMoney(10 * MoneyConstants.gold)) {
            sendSaveEmblemResult(session, GuildEmblemError.NotEnoughMoney); // "You can't afford to do that."
        } else {
            player.modifyMoney(-(long) 10 * MoneyConstants.gold);

            m_emblemInfo = emblemInfo;
            m_emblemInfo.saveToDB(m_id);

            sendSaveEmblemResult(session, GuildEmblemError.success); // "Guild Emblem saved."

            sendQueryResponse(session);
        }
    }

    public final void handleSetNewGuildMaster(WorldSession session, String name, boolean isSelfPromote) {
        var player = session.getPlayer();
        var oldGuildMaster = getMember(getLeaderGUID());

        Member newGuildMaster;

        if (isSelfPromote) {
            newGuildMaster = getMember(player.getGUID());

            if (newGuildMaster == null) {
                return;
            }

            var oldRank = getRankInfo(newGuildMaster.getRankId());

            // only second highest rank can take over guild
            if (oldRank.getOrder() != GuildRankOrder.forValue(1) || oldGuildMaster.getInactiveDays() < GuildConst.MasterDethroneInactiveDays) {
                sendCommandResult(session, GuildCommandType.ChangeLeader, GuildCommandError.Permissions);

                return;
            }
        } else {
            if (!_IsLeader(player)) {
                sendCommandResult(session, GuildCommandType.ChangeLeader, GuildCommandError.Permissions);

                return;
            }

            newGuildMaster = getMember(name);

            if (newGuildMaster == null) {
                return;
            }
        }

        SQLTransaction trans = new SQLTransaction();

        _SetLeader(trans, newGuildMaster);
        oldGuildMaster.changeRank(trans, _GetLowestRankId());

        sendEventNewLeader(newGuildMaster, oldGuildMaster, isSelfPromote);

        DB.characters.CommitTransaction(trans);
    }

    public final void handleSetBankTabInfo(WorldSession session, byte tabId, String name, String icon) {
        var tab = getBankTab(tabId);

        if (tab == null) {
            Log.outError(LogFilter.guild, "Guild.HandleSetBankTabInfo: Player {0} trying to change bank tab info from unexisting tab {1}.", session.getPlayer().getName(), tabId);

            return;
        }

        tab.setInfo(name, icon);

        GuildEventTabModified packet = new GuildEventTabModified();
        packet.tab = tabId;
        packet.name = name;
        packet.icon = icon;
        broadcastPacket(packet);
    }

    public final void handleSetMemberNote(WorldSession session, String note, ObjectGuid guid, boolean isPublic) {
        // Player must have rights to set public/officer note
        if (!_HasRankRight(session.getPlayer(), isPublic ? GuildRankRights.EditPublicNote : GuildRankRights.EOffNote)) {
            sendCommandResult(session, GuildCommandType.EditPublicNote, GuildCommandError.Permissions);
        }

        var member = getMember(guid);

        if (member != null) {
            if (isPublic) {
                member.setPublicNote(note);
            } else {
                member.setOfficerNote(note);
            }

            GuildMemberUpdateNote updateNote = new GuildMemberUpdateNote();
            updateNote.member = guid;
            updateNote.isPublic = isPublic;
            updateNote.note = note;
            broadcastPacket(updateNote);
        }
    }

    public final void handleSetRankInfo(WorldSession session, GuildRankId rankId, String name, GuildRankRights rights, int moneyPerDay, GuildBankRightsAndSlots[] rightsAndSlots) {
        // Only leader can modify ranks
        if (!_IsLeader(session.getPlayer())) {
            sendCommandResult(session, GuildCommandType.ChangeRank, GuildCommandError.Permissions);
        }

        var rankInfo = getRankInfo(rankId);

        if (rankInfo != null) {
            rankInfo.setName(name);
            rankInfo.setRights(rights);
            _SetRankBankMoneyPerDay(rankId, moneyPerDay * MoneyConstants.gold);

            for (var rightsAndSlot : rightsAndSlots) {
                _SetRankBankTabRightsAndSlots(rankId, rightsAndSlot);
            }

            GuildEventRankChanged packet = new GuildEventRankChanged();
            packet.rankID = (byte) rankId.getValue();
            broadcastPacket(packet);
        }
    }

    public final void handleBuyBankTab(WorldSession session, byte tabId) {
        var player = session.getPlayer();

        if (player == null) {
            return;
        }

        var member = getMember(player.getGUID());

        if (member == null) {
            return;
        }

        if (_GetPurchasedTabsSize() >= GuildConst.MaxBankTabs) {
            return;
        }

        if (tabId != _GetPurchasedTabsSize()) {
            return;
        }

        if (tabId >= GuildConst.MaxBankTabs) {
            return;
        }

        // Do not get money for bank tabs that the GM bought, we had to buy them already.
        // This is just a speedup check, GetGuildBankTabPrice will return 0.
        if (tabId < GuildConst.MaxBankTabs - 2) // 7th tab is actually the 6th
        {
            var tabCost = (long) (getGuildBankTabPrice(tabId) * MoneyConstants.gold);

            if (!player.hasEnoughMoney(tabCost)) // Should not happen, this is checked by client
            {
                return;
            }

            player.modifyMoney(-tabCost);
        }

        _CreateNewBankTab();

        broadcastPacket(new GuildEventTabAdded());

        sendPermissions(session); //Hack to force client to update permissions
    }

    public final void handleInviteMember(WorldSession session, String name) {
        var pInvitee = global.getObjAccessor().FindPlayerByName(name);

        if (pInvitee == null) {
            sendCommandResult(session, GuildCommandType.InvitePlayer, GuildCommandError.PlayerNotFound_S, name);

            return;
        }

        var player = session.getPlayer();

        // Do not show invitations from ignored players
        if (pInvitee.getSocial().hasIgnore(player.getGUID(), player.getSession().getAccountGUID())) {
            return;
        }

        if (!WorldConfig.getBoolValue(WorldCfg.AllowTwoSideInteractionGuild) && pInvitee.getTeam() != player.getTeam()) {
            sendCommandResult(session, GuildCommandType.InvitePlayer, GuildCommandError.NotAllied, name);

            return;
        }

        // Invited player cannot be in another guild
        if (pInvitee.getGuildId() != 0) {
            sendCommandResult(session, GuildCommandType.InvitePlayer, GuildCommandError.AlreadyInGuild_S, name);

            return;
        }

        // Invited player cannot be invited
        if (pInvitee.getGuildIdInvited() != 0) {
            sendCommandResult(session, GuildCommandType.InvitePlayer, GuildCommandError.AlreadyInvitedToGuild_S, name);

            return;
        }

        // Inviting player must have rights to invite
        if (!_HasRankRight(player, GuildRankRights.Invite)) {
            sendCommandResult(session, GuildCommandType.InvitePlayer, GuildCommandError.Permissions);

            return;
        }

        sendCommandResult(session, GuildCommandType.InvitePlayer, GuildCommandError.success, name);

        Log.outDebug(LogFilter.guild, "Player {0} invited {1} to join his Guild", player.getName(), name);

        pInvitee.setGuildIdInvited(m_id);
        _LogEvent(GuildEventLogTypes.InvitePlayer, player.getGUID().getCounter(), pInvitee.getGUID().getCounter());

        GuildInvite invite = new GuildInvite();

        invite.inviterVirtualRealmAddress = global.getWorldMgr().getVirtualRealmAddress();
        invite.guildVirtualRealmAddress = global.getWorldMgr().getVirtualRealmAddress();
        invite.guildGUID = getGUID();

        invite.emblemStyle = m_emblemInfo.getStyle();
        invite.emblemColor = m_emblemInfo.getColor();
        invite.borderStyle = m_emblemInfo.getBorderStyle();
        invite.borderColor = m_emblemInfo.getBorderColor();
        invite.background = m_emblemInfo.getBackgroundColor();
        invite.achievementPoints = (int) getAchievementMgr().getAchievementPoints();

        invite.inviterName = player.getName();
        invite.guildName = getName();

        var oldGuild = pInvitee.getGuild();

        if (oldGuild) {
            invite.oldGuildGUID = oldGuild.getGUID();
            invite.oldGuildName = oldGuild.getName();
            invite.oldGuildVirtualRealmAddress = global.getWorldMgr().getVirtualRealmAddress();
        }

        pInvitee.sendPacket(invite);
    }

    public final void handleAcceptMember(WorldSession session) {
        var player = session.getPlayer();

        if (!WorldConfig.getBoolValue(WorldCfg.AllowTwoSideInteractionGuild) && player.getTeam() != global.getCharacterCacheStorage().getCharacterTeamByGuid(getLeaderGUID())) {
            return;
        }

        addMember(null, player.getGUID());
    }

    public final void handleLeaveMember(WorldSession session) {
        var player = session.getPlayer();

        // If leader is leaving
        if (_IsLeader(player)) {
            if (m_members.size() > 1) {
                // Leader cannot leave if he is not the last member
                sendCommandResult(session, GuildCommandType.LeaveGuild, GuildCommandError.LeaderLeave);
            } else {
                // Guild is disbanded if leader leaves.
                disband();
            }
        } else {
            deleteMember(null, player.getGUID(), false, false);

            _LogEvent(GuildEventLogTypes.LeaveGuild, player.getGUID().getCounter());
            sendEventPlayerLeft(player);

            sendCommandResult(session, GuildCommandType.LeaveGuild, GuildCommandError.success, m_name);
        }

        global.getCalendarMgr().removePlayerGuildEventsAndSignups(player.getGUID(), getId());
    }

    public final void handleRemoveMember(WorldSession session, ObjectGuid guid) {
        var player = session.getPlayer();

        // Player must have rights to remove members
        if (!_HasRankRight(player, GuildRankRights.Remove)) {
            sendCommandResult(session, GuildCommandType.RemovePlayer, GuildCommandError.Permissions);
        }

        var member = getMember(guid);

        if (member != null) {
            var name = member.getName();

            // Guild masters cannot be removed
            if (member.isRank(GuildRankId.GuildMaster)) {
                sendCommandResult(session, GuildCommandType.RemovePlayer, GuildCommandError.LeaderLeave);
            }
            // Do not allow to remove player with the same rank or higher
            else {
                var memberMe = getMember(player.getGUID());
                var myRank = getRankInfo(memberMe.getRankId());
                var targetRank = getRankInfo(member.getRankId());

                if (memberMe == null || targetRank.getOrder() <= myRank.getOrder()) {
                    sendCommandResult(session, GuildCommandType.RemovePlayer, GuildCommandError.RankTooHigh_S, name);
                } else {
                    deleteMember(null, guid, false, true);
                    _LogEvent(GuildEventLogTypes.UninvitePlayer, player.getGUID().getCounter(), guid.getCounter());

                    var pMember = global.getObjAccessor().findConnectedPlayer(guid);
                    sendEventPlayerLeft(pMember, player, true);

                    sendCommandResult(session, GuildCommandType.RemovePlayer, GuildCommandError.success, name);
                }
            }
        }
    }

    public final void handleUpdateMemberRank(WorldSession session, ObjectGuid guid, boolean demote) {
        var player = session.getPlayer();
        var type = demote ? GuildCommandType.DemotePlayer : GuildCommandType.PromotePlayer;
        // Player must have rights to promote
        Member member;

        if (!_HasRankRight(player, demote ? GuildRankRights.Demote : GuildRankRights.promote)) {
            sendCommandResult(session, type, GuildCommandError.LeaderLeave);
        }
        // Promoted player must be a member of guild
        else if ((member = getMember(guid)) != null) {
            var name = member.getName();

            // Player cannot promote himself
            if (member.isSamePlayer(player.getGUID())) {
                sendCommandResult(session, type, GuildCommandError.NameInvalid);

                return;
            }

            var memberMe = getMember(player.getGUID());
            var myRank = getRankInfo(memberMe.getRankId());
            var oldRank = getRankInfo(member.getRankId());
            GuildRankId newRankId;

            if (demote) {
                // Player can demote only lower rank members
                if (oldRank.getOrder() <= myRank.getOrder()) {
                    sendCommandResult(session, type, GuildCommandError.RankTooHigh_S, name);

                    return;
                }

                // Lowest rank cannot be demoted
                var newRank = getRankInfo(oldRank.getOrder() + 1);

                if (newRank == null) {
                    sendCommandResult(session, type, GuildCommandError.RankTooLow_S, name);

                    return;
                }

                newRankId = newRank.getId();
            } else {
                // Allow to promote only to lower rank than member's rank
                // memberMe.getRankId() + 1 is the highest rank that current player can promote to
                if ((oldRank.getOrder() - 1) <= myRank.getOrder()) {
                    sendCommandResult(session, type, GuildCommandError.RankTooHigh_S, name);

                    return;
                }

                newRankId = getRankInfo((oldRank.getOrder() - 1)).getId();
            }

            member.changeRank(null, newRankId);
            _LogEvent(demote ? GuildEventLogTypes.DemotePlayer : GuildEventLogTypes.PromotePlayer, player.getGUID().getCounter(), member.getGUID().getCounter(), (byte) newRankId.getValue());
            //_BroadcastEvent(demote ? GuildEvents.Demotion : GuildEvents.Promotion, ObjectGuid.Empty, player.getName(), name, _GetRankName((byte)newRankId));
        }
    }

    public final void handleSetMemberRank(WorldSession session, ObjectGuid targetGuid, ObjectGuid setterGuid, GuildRankOrder rank) {
        var player = session.getPlayer();
        var member = getMember(targetGuid);
        var rights = GuildRankRights.promote;
        var type = GuildCommandType.PromotePlayer;

        var oldRank = getRankInfo(member.getRankId());
        var newRank = getRankInfo(rank);

        if (oldRank == null || newRank == null) {
            return;
        }

        if (rank.getValue() > oldRank.getOrder()) {
            rights = GuildRankRights.Demote;
            type = GuildCommandType.DemotePlayer;
        }

        // Promoted player must be a member of guild
        if (!_HasRankRight(player, rights)) {
            sendCommandResult(session, type, GuildCommandError.Permissions);

            return;
        }

        // Player cannot promote himself
        if (member.isSamePlayer(player.getGUID())) {
            sendCommandResult(session, type, GuildCommandError.NameInvalid);

            return;
        }

        sendGuildRanksUpdate(setterGuid, targetGuid, newRank.getId());
    }

    public final void handleAddNewRank(WorldSession session, String name) {
        var size = _GetRanksSize();

        if (size >= GuildConst.MaxRanks) {
            return;
        }

        // Only leader can add new rank
        if (_IsLeader(session.getPlayer())) {
            if (_CreateRank(null, name, GuildRankRights.GChatListen.getValue() | GuildRankRights.GChatSpeak.getValue())) {
                broadcastPacket(new GuildEventRanksUpdated());
            }
        }
    }

    public final void handleRemoveRank(WorldSession session, GuildRankOrder rankOrder) {
        // Cannot remove rank if total count is minimum allowed by the client or is not leader
        if (_GetRanksSize() <= GuildConst.MinRanks || !_IsLeader(session.getPlayer())) {
            return;
        }

        var rankInfo = tangible.ListHelper.find(m_ranks, rank -> rank.getOrder() == rankOrder);

        if (rankInfo == null) {
            return;
        }

        var trans = new SQLTransaction();

        // Delete bank rights for rank
        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GUILD_BANK_RIGHTS_FOR_RANK);
        stmt.AddValue(0, m_id);
        stmt.AddValue(1, (byte) rankInfo.getId().getValue());
        trans.append(stmt);

        // Delete rank
        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GUILD_RANK);
        stmt.AddValue(0, m_id);
        stmt.AddValue(1, (byte) rankInfo.getId().getValue());
        trans.append(stmt);

        m_ranks.remove(rankInfo);

        // correct order of other ranks
        for (var otherRank : m_ranks) {
            if (otherRank.getOrder() < rankOrder.getValue()) {
                continue;
            }

            otherRank.setOrder(otherRank.getOrder() - 1);

            stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GUILD_RANK_ORDER);
            stmt.AddValue(0, (byte) otherRank.getOrder().getValue());
            stmt.AddValue(1, (byte) otherRank.getId().getValue());
            stmt.AddValue(2, m_id);
            trans.append(stmt);
        }

        DB.characters.CommitTransaction(trans);

        broadcastPacket(new GuildEventRanksUpdated());
    }

    public final void handleShiftRank(WorldSession session, GuildRankOrder rankOrder, boolean shiftUp) {
        // Only leader can modify ranks
        if (!_IsLeader(session.getPlayer())) {
            return;
        }

        var otherRankOrder = GuildRankOrder.forValue(rankOrder + (shiftUp ? -1 : 1));

        var rankInfo = getRankInfo(rankOrder);
        var otherRankInfo = getRankInfo(otherRankOrder);

        if (rankInfo == null || otherRankInfo == null) {
            return;
        }

        // can't shift guild master rank (rank id = 0) - there's already a client-side limitation for it so that's just a safe-guard
        if (rankInfo.getId() == GuildRankId.GuildMaster || otherRankInfo.getId() == GuildRankId.GuildMaster) {
            return;
        }

        rankInfo.setOrder(otherRankOrder);
        otherRankInfo.setOrder(rankOrder);

        var trans = new SQLTransaction();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GUILD_RANK_ORDER);
        stmt.AddValue(0, (byte) rankInfo.getOrder().getValue());
        stmt.AddValue(1, (byte) rankInfo.getId().getValue());
        stmt.AddValue(2, m_id);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GUILD_RANK_ORDER);
        stmt.AddValue(0, (byte) otherRankInfo.getOrder().getValue());
        stmt.AddValue(1, (byte) otherRankInfo.getId().getValue());
        stmt.AddValue(2, m_id);
        trans.append(stmt);

        DB.characters.CommitTransaction(trans);

        // force client to re-request SMSG_GUILD_RANKS
        broadcastPacket(new GuildEventRanksUpdated());
    }

    public final void handleMemberDepositMoney(WorldSession session, long amount) {
        handleMemberDepositMoney(session, amount, false);
    }

    public final void handleMemberDepositMoney(WorldSession session, long amount, boolean cashFlow) {
        // guild bank cannot have more than MAX_MONEY_AMOUNT
        amount = Math.min(amount, PlayerConst.MaxMoneyAmount - m_bankMoney);

        if (amount == 0) {
            return;
        }

        var player = session.getPlayer();

        // Call script after validation and before money transfer.
        global.getScriptMgr().<IGuildOnMemberDepositMoney>ForEach(p -> p.OnMemberDepositMoney(this, player, amount));

        if (m_bankMoney > GuildConst.MoneyLimit - amount) {
            if (!cashFlow) {
                sendCommandResult(session, GuildCommandType.MoveItem, GuildCommandError.TooMuchMoney);
            }

            return;
        }

        SQLTransaction trans = new SQLTransaction();
        _ModifyBankMoney(trans, amount, true);

        if (!cashFlow) {
            player.modifyMoney(-(long) amount);
            player.saveGoldToDB(trans);
        }

        _LogBankEvent(trans, cashFlow ? GuildBankEventLogTypes.CashFlowDeposit : GuildBankEventLogTypes.DepositMoney, (byte) 0, player.getGUID().getCounter(), (int) amount);
        DB.characters.CommitTransaction(trans);

        sendEventBankMoneyChanged();

        if (player.getSession().hasPermission(RBACPermissions.LogGmTrade)) {
            Log.outCommand(player.getSession().getAccountId(), "GM {0} (Account: {1}) deposit money (Amount: {2}) to guild bank (Guild ID {3})", player.getName(), player.getSession().getAccountId(), amount, m_id);
        }
    }

    public final boolean handleMemberWithdrawMoney(WorldSession session, long amount) {
        return handleMemberWithdrawMoney(session, amount, false);
    }

    public final boolean handleMemberWithdrawMoney(WorldSession session, long amount, boolean repair) {
        // clamp amount to MAX_MONEY_AMOUNT, Players can't hold more than that anyway
        amount = Math.min(amount, PlayerConst.MaxMoneyAmount);

        if (m_bankMoney < amount) // Not enough money in bank
        {
            return false;
        }

        var player = session.getPlayer();

        var member = getMember(player.getGUID());

        if (member == null) {
            return false;
        }

        if (!_HasRankRight(player, repair ? GuildRankRights.WithdrawRepair : GuildRankRights.WithdrawGold)) {
            return false;
        }

        if (_GetMemberRemainingMoney(member) < (long) amount) // Check if we have enough slot/money today
        {
            return false;
        }

        // Call script after validation and before money transfer.
        global.getScriptMgr().<IGuildOnMemberWithDrawMoney>ForEach(p -> p.OnMemberWitdrawMoney(this, player, amount, repair));

        SQLTransaction trans = new SQLTransaction();

        // Add money to player (if required)
        if (!repair) {
            if (!player.modifyMoney((long) amount)) {
                return false;
            }

            player.saveGoldToDB(trans);
        }

        // Update remaining money amount
        member.updateBankMoneyWithdrawValue(trans, amount);
        // Remove money from bank
        _ModifyBankMoney(trans, amount, false);

        // Log guild bank event
        _LogBankEvent(trans, repair ? GuildBankEventLogTypes.RepairMoney : GuildBankEventLogTypes.WithdrawMoney, (byte) 0, player.getGUID().getCounter(), (int) amount);
        DB.characters.CommitTransaction(trans);

        sendEventBankMoneyChanged();

        return true;
    }

    public final void handleMemberLogout(WorldSession session) {
        var player = session.getPlayer();
        var member = getMember(player.getGUID());

        if (member != null) {
            member.setStats(player);
            member.updateLogoutTime();
            member.resetFlags();
        }

        sendEventPresenceChanged(session, false, true);
        saveToDB();
    }

    public final void handleDelete(WorldSession session) {
        // Only leader can disband guild
        if (_IsLeader(session.getPlayer())) {
            disband();
            Log.outDebug(LogFilter.guild, "Guild Successfully Disbanded");
        }
    }

    public final void handleGuildPartyRequest(WorldSession session) {
        var player = session.getPlayer();
        var group = player.getGroup();

        // Make sure player is a member of the guild and that he is in a group.
        if (!isMember(player.getGUID()) || !group) {
            return;
        }

        GuildPartyState partyStateResponse = new GuildPartyState();
        partyStateResponse.inGuildParty = (player.getMap().getOwnerGuildId(player.getTeam()) == getId());
        partyStateResponse.numMembers = 0;
        partyStateResponse.numRequired = 0;
        partyStateResponse.guildXPEarnedMult = 0.0f;
        session.sendPacket(partyStateResponse);
    }

    public final void handleGuildRequestChallengeUpdate(WorldSession session) {
        GuildChallengeUpdate updatePacket = new GuildChallengeUpdate();

        for (var i = 0; i < GuildConst.ChallengesTypes; ++i) {
            updatePacket.CurrentCount[i] = 0; // @todo current count
        }

        for (var i = 0; i < GuildConst.ChallengesTypes; ++i) {
            updatePacket.MaxCount[i] = GuildConst.ChallengesMaxCount[i];
        }

        for (var i = 0; i < GuildConst.ChallengesTypes; ++i) {
            updatePacket.MaxLevelGold[i] = GuildConst.ChallengeMaxLevelGoldReward[i];
        }

        for (var i = 0; i < GuildConst.ChallengesTypes; ++i) {
            updatePacket.Gold[i] = GuildConst.ChallengeGoldReward[i];
        }

        session.sendPacket(updatePacket);
    }

    public final void sendEventLog(WorldSession session) {
        var eventLog = m_eventLog.getGuildLog();

        GuildEventLogQueryResults packet = new GuildEventLogQueryResults();

        for (var entry : eventLog) {
            entry.writePacket(packet);
        }

        session.sendPacket(packet);
    }

    public final void sendNewsUpdate(WorldSession session) {
        var newsLog = m_newsLog.getGuildLog();

        GuildNewsPkt packet = new GuildNewsPkt();

        for (var newsLogEntry : newsLog) {
            newsLogEntry.writePacket(packet);
        }

        session.sendPacket(packet);
    }

    public final void sendBankLog(WorldSession session, byte tabId) {
        // GuildConst.MaxBankTabs send by client for money log
        if (tabId < _GetPurchasedTabsSize() || tabId == GuildConst.MaxBankTabs) {
            var bankEventLog = m_bankEventLog[tabId].getGuildLog();

            GuildBankLogQueryResults packet = new GuildBankLogQueryResults();
            packet.tab = tabId;

            //if (tabId == GUILD_BANK_MAX_TABS && hasCashFlow)
            //    packet.weeklyBonusMoney.set(uint64(weeklyBonusMoney));

            for (var entry : bankEventLog) {
                entry.writePacket(packet);
            }

            session.sendPacket(packet);
        }
    }

    public final void sendBankTabText(WorldSession session, byte tabId) {
        var tab = getBankTab(tabId);

        if (tab != null) {
            tab.sendText(this, session);
        }
    }

    public final void sendPermissions(WorldSession session) {
        var member = getMember(session.getPlayer().getGUID());

        if (member == null) {
            return;
        }

        var rankId = member.getRankId();

        GuildPermissionsQueryResults queryResult = new GuildPermissionsQueryResults();
        queryResult.rankID = (byte) rankId.getValue();
        queryResult.withdrawGoldLimit = (int) _GetMemberRemainingMoney(member);
        queryResult.flags = _GetRankRights(rankId).getValue();
        queryResult.numTabs = _GetPurchasedTabsSize();

        for (byte tabId = 0; tabId < GuildConst.MaxBankTabs; ++tabId) {
            GuildPermissionsQueryResults.GuildRankTabPermissions tabPerm = new GuildPermissionsQueryResults.GuildRankTabPermissions();
            tabPerm.flags = _GetRankBankTabRights(rankId, tabId).getValue();
            tabPerm.withdrawItemLimit = _GetMemberRemainingSlots(member, tabId);
            queryResult.tab.add(tabPerm);
        }

        session.sendPacket(queryResult);
    }

    public final void sendMoneyInfo(WorldSession session) {
        var member = getMember(session.getPlayer().getGUID());

        if (member == null) {
            return;
        }

        var amount = _GetMemberRemainingMoney(member);

        GuildBankRemainingWithdrawMoney packet = new GuildBankRemainingWithdrawMoney();
        packet.remainingWithdrawMoney = amount;
        session.sendPacket(packet);
    }

    public final void sendLoginInfo(WorldSession session) {
        var player = session.getPlayer();
        var member = getMember(player.getGUID());

        if (member == null) {
            return;
        }

        sendEventMOTD(session);
        sendGuildRankInfo(session);
        sendEventPresenceChanged(session, true, true); // Broadcast

        // Send to self separately, player is not in world yet and is not found by _BroadcastEvent
        sendEventPresenceChanged(session, true);

        if (Objects.equals(member.getGUID(), getLeaderGUID())) {
            GuildFlaggedForRename renameFlag = new GuildFlaggedForRename();
            renameFlag.flagSet = false;
            player.sendPacket(renameFlag);
        }

        for (var entry : CliDB.GuildPerkSpellsStorage.values()) {
            player.learnSpell(entry.spellID, true);
        }

        getAchievementMgr().sendAllData(player);

        // tells the client to request bank withdrawal limit
        player.sendPacket(new GuildMemberDailyReset());

        member.setStats(player);
        member.addFlag(GuildMemberFlags.online);
    }

    public final void sendEventAwayChanged(ObjectGuid memberGuid, boolean afk, boolean dnd) {
        var member = getMember(memberGuid);

        if (member == null) {
            return;
        }

        if (afk) {
            member.addFlag(GuildMemberFlags.AFK);
        } else {
            member.removeFlag(GuildMemberFlags.AFK);
        }

        if (dnd) {
            member.addFlag(GuildMemberFlags.DND);
        } else {
            member.removeFlag(GuildMemberFlags.DND);
        }

        GuildEventStatusChange statusChange = new GuildEventStatusChange();
        statusChange.guid = memberGuid;
        statusChange.AFK = afk;
        statusChange.DND = dnd;
        broadcastPacket(statusChange);
    }

    public final boolean loadFromDB(SQLFields fields) {
        m_id = fields.<Integer>Read(0);
        m_name = fields.<String>Read(1);
        m_leaderGuid = ObjectGuid.create(HighGuid.Player, fields.<Long>Read(2));

        if (!m_emblemInfo.loadFromDB(fields)) {
            Log.outError(LogFilter.guild, "Guild {0} has invalid emblem colors (Background: {1}, Border: {2}, Emblem: {3}), skipped.", m_id, m_emblemInfo.getBackgroundColor(), m_emblemInfo.getBorderColor(), m_emblemInfo.getColor());

            return false;
        }

        m_info = fields.<String>Read(8);
        m_motd = fields.<String>Read(9);
        m_createdDate = fields.<Integer>Read(10);
        m_bankMoney = fields.<Long>Read(11);

        var purchasedTabs = (byte) fields.<Integer>Read(12);

        if (purchasedTabs > GuildConst.MaxBankTabs) {
            purchasedTabs = GuildConst.MaxBankTabs;
        }

        m_bankTabs.clear();

        for (byte i = 0; i < purchasedTabs; ++i) {
            m_bankTabs.add(new bankTab(m_id, i));
        }

        return true;
    }

    public final void loadRankFromDB(SQLFields field) {
        RankInfo rankInfo = new RankInfo(m_id);

        rankInfo.loadFromDB(field);

        m_ranks.add(rankInfo);
    }

    public final boolean loadMemberFromDB(SQLFields field) {
        var lowguid = field.<Long>Read(1);
        var playerGuid = ObjectGuid.create(HighGuid.Player, lowguid);

        Member member = new member(m_id, playerGuid, GuildRankId.forValue(field.<Byte>Read(2)));
        var isNew = m_members.TryAdd(playerGuid, member);

        if (!isNew) {
            Log.outError(LogFilter.guild, String.format("Tried to add %1$s to guild '%2$s'. Member already exists.", playerGuid, m_name));

            return false;
        }

        if (!member.loadFromDB(field)) {
            _DeleteMemberFromDB(null, lowguid);

            return false;
        }

        global.getCharacterCacheStorage().updateCharacterGuildId(playerGuid, getId());
        m_members.put(member.getGUID(), member);

        return true;
    }

    public final void loadBankRightFromDB(SQLFields field) {
        // tabId              rights                slots
        GuildBankRightsAndSlots rightsAndSlots = new GuildBankRightsAndSlots(field.<Byte>Read(1), field.<Byte>Read(3), field.<Integer>Read(4));
        // rankId
        _SetRankBankTabRightsAndSlots(GuildRankId.forValue(field.<Byte>Read(2)), rightsAndSlots, false);
    }

    public final boolean loadEventLogFromDB(SQLFields field) {
        if (m_eventLog.canInsert()) {
            m_eventLog.loadEvent(new EventLogEntry(m_id, field.<Integer>Read(1), field.<Long>Read(6), GuildEventLogTypes.forValue(field.<Byte>Read(2)), field.<Long>Read(3), field.<Long>Read(4), field.<Byte>Read(5))); // rank

            return true;
        }

        return false;
    }

    public final boolean loadBankEventLogFromDB(SQLFields field) {
        var dbTabId = field.<Byte>Read(1);
        var isMoneyTab = (dbTabId == GuildConst.BankMoneyLogsTab);

        if (dbTabId < _GetPurchasedTabsSize() || isMoneyTab) {
            var tabId = isMoneyTab ? (byte) GuildConst.MaxBankTabs : dbTabId;
            var pLog = m_bankEventLog[tabId];

            if (pLog.canInsert()) {
                var guid = field.<Integer>Read(2);
                var eventType = GuildBankEventLogTypes.forValue(field.<Byte>Read(3));

                if (BankEventLogEntry.isMoneyEvent(eventType)) {
                    if (!isMoneyTab) {
                        Log.outError(LogFilter.guild, "GuildBankEventLog ERROR: MoneyEvent(LogGuid: {0}, Guild: {1}) does not belong to money tab ({2}), ignoring...", guid, m_id, dbTabId);

                        return false;
                    }
                } else if (isMoneyTab) {
                    Log.outError(LogFilter.guild, "GuildBankEventLog ERROR: non-money event (LogGuid: {0}, Guild: {1}) belongs to money tab, ignoring...", guid, m_id);

                    return false;
                }

                pLog.loadEvent(new BankEventLogEntry(m_id, guid, field.<Long>Read(8), dbTabId, eventType, field.<Long>Read(4), field.<Long>Read(5), field.<SHORT>Read(6), field.<Byte>Read(7))); // dest tab id
            }
        }

        return true;
    }

    public final void loadGuildNewsLogFromDB(SQLFields field) {
        if (!m_newsLog.canInsert()) {
            return;
        }

        var news = new NewsLogEntry(m_id, field.<Integer>Read(1), field.<Long>Read(6), GuildNews.forValue(field.<Byte>Read(2)), ObjectGuid.create(HighGuid.Player, field.<Long>Read(3)), field.<Integer>Read(4), field.<Integer>Read(5)); // value)

        m_newsLog.loadEvent(news);
    }

    public final void loadBankTabFromDB(SQLFields field) {
        var tabId = field.<Byte>Read(1);

        if (tabId >= _GetPurchasedTabsSize()) {
            Log.outError(LogFilter.guild, "Invalid tab (tabId: {0}) in guild bank, skipped.", tabId);
        } else {
            m_bankTabs.get(tabId).loadFromDB(field);
        }
    }

    public final boolean loadBankItemFromDB(SQLFields field) {
        var tabId = field.<Byte>Read(52);

        if (tabId >= _GetPurchasedTabsSize()) {
            Log.outError(LogFilter.guild, "Invalid tab for item (GUID: {0}, id: {1}) in guild bank, skipped.", field.<Integer>Read(0), field.<Integer>Read(1));

            return false;
        }

        return m_bankTabs.get(tabId).loadItemFromDB(field);
    }

    public final boolean validate() {
        // Validate ranks data
        // GUILD RANKS represent a sequence starting from 0 = GUILD_MASTER (ALL PRIVILEGES) to max 9 (lowest privileges).
        // The lower rank id is considered higher rank - so promotion does rank-- and demotion does rank++
        // Between ranks in sequence cannot be gaps - so 0, 1, 2, 4 is impossible
        // Min ranks count is 2 and max is 10.
        var broken_ranks = false;
        var ranks = _GetRanksSize();

        SQLTransaction trans = new SQLTransaction();

        if (ranks < GuildConst.MinRanks || ranks > GuildConst.MaxRanks) {
            Log.outError(LogFilter.guild, "Guild {0} has invalid number of ranks, creating new...", m_id);
            broken_ranks = true;
        } else {
            for (byte rankId = 0; rankId < ranks; ++rankId) {
                var rankInfo = getRankInfo(GuildRankId.forValue(rankId));

                if (rankInfo.getId() != GuildRankId.forValue(rankId)) {
                    Log.outError(LogFilter.guild, "Guild {0} has broken rank id {1}, creating default set of ranks...", m_id, rankId);
                    broken_ranks = true;
                } else {
                    rankInfo.createMissingTabsIfNeeded(_GetPurchasedTabsSize(), trans, true);
                }
            }
        }

        if (broken_ranks) {
            m_ranks.clear();
            _CreateDefaultGuildRanks(trans, SharedConst.DefaultLocale);
        }

        // Validate members' data
        for (var member : m_members.values()) {
            if (getRankInfo(member.getRankId()) == null) {
                member.changeRank(trans, _GetLowestRankId());
            }
        }

        // Repair the structure of the guild.
        // If the guildmaster doesn't exist or isn't member of the guild
        // attempt to promote another member.
        var leader = getMember(m_leaderGuid);

        if (leader == null) {
            deleteMember(trans, m_leaderGuid);

            // If no more members left, disband guild
            if (m_members.isEmpty()) {
                disband();

                return false;
            }
        } else if (!leader.isRank(GuildRankId.GuildMaster)) {
            _SetLeader(trans, leader);
        }

        if (trans.commands.count > 0) {
            DB.characters.CommitTransaction(trans);
        }

        _UpdateAccountsNumber();

        return true;
    }

    public final void broadcastToGuild(WorldSession session, boolean officerOnly, String msg, Language language) {
        if (session != null && session.getPlayer() != null && _HasRankRight(session.getPlayer(), officerOnly ? GuildRankRights.OffChatSpeak : GuildRankRights.GChatSpeak)) {
            ChatPkt data = new ChatPkt();
            data.initialize(officerOnly ? ChatMsg.Officer : ChatMsg.guild, language, session.getPlayer(), null, msg);

            for (var member : m_members.values()) {
                var player = member.findPlayer();

                if (player != null) {
                    if (player.session != null && _HasRankRight(player, officerOnly ? GuildRankRights.OffChatListen : GuildRankRights.GChatListen) && !player.Social.hasIgnore(session.getPlayer().getGUID(), session.getAccountGUID())) {
                        player.sendPacket(data);
                    }
                }
            }
        }
    }

    public final void broadcastAddonToGuild(WorldSession session, boolean officerOnly, String msg, String prefix, boolean isLogged) {
        if (session != null && session.getPlayer() != null && _HasRankRight(session.getPlayer(), officerOnly ? GuildRankRights.OffChatSpeak : GuildRankRights.GChatSpeak)) {
            ChatPkt data = new ChatPkt();
            data.initialize(officerOnly ? ChatMsg.Officer : ChatMsg.guild, isLogged ? language.AddonLogged : language.Addon, session.getPlayer(), null, msg, 0, "", locale.enUS, prefix);

            for (var member : m_members.values()) {
                var player = member.findPlayer();

                if (player) {
                    if (player.session != null && _HasRankRight(player, officerOnly ? GuildRankRights.OffChatListen : GuildRankRights.GChatListen) && !player.Social.hasIgnore(session.getPlayer().getGUID(), session.getAccountGUID()) && player.session.isAddonRegistered(prefix)) {
                        player.sendPacket(data);
                    }
                }
            }
        }
    }

    public final void broadcastPacketToRank(ServerPacket packet, GuildRankId rankId) {
        for (var member : m_members.values()) {
            if (member.isRank(rankId)) {
                var player = member.findPlayer();

                if (player != null) {
                    player.sendPacket(packet);
                }
            }
        }
    }

    public final void broadcastPacket(ServerPacket packet) {
        for (var member : m_members.values()) {
            var player = member.findPlayer();

            if (player != null) {
                player.sendPacket(packet);
            }
        }
    }

    public final void broadcastPacketIfTrackingAchievement(ServerPacket packet, int criteriaId) {
        for (var member : m_members.values()) {
            if (member.isTrackingCriteriaId(criteriaId)) {
                var player = member.findPlayer();

                if (player) {
                    player.sendPacket(packet);
                }
            }
        }
    }

    public final void massInviteToEvent(WorldSession session, int minLevel, int maxLevel, GuildRankOrder minRank) {
        CalendarCommunityInvite packet = new CalendarCommunityInvite();


        for (var(guid, member) : m_members) {
            // not sure if needed, maybe client checks it as well
            if (packet.invites.size() >= SharedConst.CalendarMaxInvites) {
                var player = session.getPlayer();

                if (player != null) {
                    global.getCalendarMgr().sendCalendarCommandResult(player.getGUID(), CalendarError.InvitesExceeded);
                }

                return;
            }

            if (Objects.equals(guid, session.getPlayer().getGUID())) {
                continue;
            }

            int level = global.getCharacterCacheStorage().getCharacterLevelByGuid(guid);

            if (level < minLevel || level > maxLevel) {
                continue;
            }

            var rank = getRankInfo(member.getRankId());

            if (rank.getOrder() > minRank.getValue()) {
                continue;
            }

            packet.invites.add(new CalendarEventInitialInviteInfo(guid, (byte) level));
        }

        session.sendPacket(packet);
    }

    public final boolean addMember(SQLTransaction trans, ObjectGuid guid) {
        return addMember(trans, guid, null);
    }

    public final boolean addMember(SQLTransaction trans, ObjectGuid guid, GuildRankId rankId) {
        var player = global.getObjAccessor().findPlayer(guid);

        // Player cannot be in guild
        if (player != null) {
            if (player.getGuildId() != 0) {
                return false;
            }
        } else if (global.getCharacterCacheStorage().getCharacterGuildIdByGuid(guid) != 0) {
            return false;
        }

        // Remove all player signs from another petitions
        // This will be prevent attempt to join many guilds and corrupt guild data integrity
        player.removePetitionsAndSigns(guid);

        var lowguid = guid.getCounter();

        // If rank was not passed, assign lowest possible rank
        if (!rankId != null) {
            rankId = _GetLowestRankId();
        }

        Member member = new member(m_id, guid, rankId);
        var isNew = m_members.TryAdd(guid, member);

        if (!isNew) {
            Log.outError(LogFilter.guild, String.format("Tried to add %1$s to guild '%2$s'. Member already exists.", guid, m_name));

            return false;
        }

        var name = "";

        if (player != null) {
            m_members.put(guid, member);
            player.setInGuild(m_id);
            player.setGuildIdInvited(0);
            player.setGuildRank((byte) rankId);
            player.setGuildLevel(getLevel());
            member.setStats(player);
            sendLoginInfo(player.getSession());
            name = player.getName();
        } else {
            member.resetFlags();

            var ok = false;
            // Player must exist
            var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHAR_DATA_FOR_GUILD);
            stmt.AddValue(0, lowguid);
            var result = DB.characters.query(stmt);

            if (!result.isEmpty()) {
                name = result.<String>Read(0);

                member.setStats(name, result.<Byte>Read(1), race.forValue(result.<Byte>Read(2)), UnitClass.forValue(result.<Byte>Read(3)), gender.forValue(result.<Byte>Read(4)), result.<SHORT>Read(5), result.<Integer>Read(6), 0);

                ok = member.checkStats();
            }

            if (!ok) {
                return false;
            }

            m_members.put(guid, member);
            global.getCharacterCacheStorage().updateCharacterGuildId(guid, getId());
        }

        member.saveToDB(trans);

        _UpdateAccountsNumber();
        _LogEvent(GuildEventLogTypes.JoinGuild, lowguid);

        GuildEventPlayerJoined joinNotificationPacket = new GuildEventPlayerJoined();
        joinNotificationPacket.guid = guid;
        joinNotificationPacket.name = name;
        joinNotificationPacket.virtualRealmAddress = global.getWorldMgr().getVirtualRealmAddress();
        broadcastPacket(joinNotificationPacket);

        // Call scripts if member was succesfully added (and stored to database)
        global.getScriptMgr().<IGuildOnAddMember>ForEach(p -> p.onAddMember(this, player, (byte) rankId));

        return true;
    }

    public final void deleteMember(SQLTransaction trans, ObjectGuid guid, boolean isDisbanding, boolean isKicked) {
        deleteMember(trans, guid, isDisbanding, isKicked, false);
    }

    public final void deleteMember(SQLTransaction trans, ObjectGuid guid, boolean isDisbanding) {
        deleteMember(trans, guid, isDisbanding, false, false);
    }

    public final void deleteMember(SQLTransaction trans, ObjectGuid guid) {
        deleteMember(trans, guid, false, false, false);
    }

    public final void deleteMember(SQLTransaction trans, ObjectGuid guid, boolean isDisbanding, boolean isKicked, boolean canDeleteGuild) {
        var player = global.getObjAccessor().findPlayer(guid);

        // Guild master can be deleted when loading guild and guid doesn't exist in character table
        // or when he is removed from guild by gm command
        if (Objects.equals(m_leaderGuid, guid) && !isDisbanding) {
            Member oldLeader = null;
            Member newLeader = null;


            for (var(memberGuid, member) : m_members) {
                if (Objects.equals(memberGuid, guid)) {
                    oldLeader = member;
                } else if (newLeader == null || newLeader.getRankId() > member.getRankId()) {
                    newLeader = member;
                }
            }

            if (newLeader == null) {
                disband();

                return;
            }

            _SetLeader(trans, newLeader);

            // If leader does not exist (at guild loading with deleted leader) do not send broadcasts
            if (oldLeader != null) {
                sendEventNewLeader(newLeader, oldLeader, true);
                sendEventPlayerLeft(player);
            }
        }

        // Call script on remove before member is actually removed from guild (and database)
        global.getScriptMgr().<IGuildOnRemoveMember>ForEach(p -> p.onRemoveMember(this, player, isDisbanding, isKicked));

        m_members.remove(guid);

        // If player not online data in data field will be loaded from guild tabs no need to update it !!
        if (player != null) {
            player.setInGuild(0);
            player.setGuildRank((byte) 0);
            player.setGuildLevel(0);

            for (var entry : CliDB.GuildPerkSpellsStorage.values()) {
                player.removeSpell(entry.spellID, false, false);
            }
        } else {
            global.getCharacterCacheStorage().updateCharacterGuildId(guid, 0);
        }

        _DeleteMemberFromDB(trans, guid.getCounter());

        if (!isDisbanding) {
            _UpdateAccountsNumber();
        }
    }

    public final boolean changeMemberRank(SQLTransaction trans, ObjectGuid guid, GuildRankId newRank) {
        if (getRankInfo(newRank) != null) // Validate rank (allow only existing ranks)
        {
            var member = getMember(guid);

            if (member != null) {
                member.changeRank(trans, newRank);

                return true;
            }
        }

        return false;
    }

    public final boolean isMember(ObjectGuid guid) {
        return m_members.containsKey(guid);
    }

    public final long getMemberAvailableMoneyForRepairItems(ObjectGuid guid) {
        var member = getMember(guid);

        if (member == null) {
            return 0;
        }

        return Math.min(m_bankMoney, (long) _GetMemberRemainingMoney(member));
    }

    public final void swapItems(Player player, byte tabId, byte slotId, byte destTabId, byte destSlotId, int splitedAmount) {
        if (tabId >= _GetPurchasedTabsSize() || slotId >= GuildConst.MaxBankSlots || destTabId >= _GetPurchasedTabsSize() || destSlotId >= GuildConst.MaxBankSlots) {
            return;
        }

        if (tabId == destTabId && slotId == destSlotId) {
            return;
        }

        BankMoveItemData from = new BankMoveItemData(this, player, tabId, slotId);
        BankMoveItemData to = new BankMoveItemData(this, player, destTabId, destSlotId);
        _MoveItems(from, to, splitedAmount);
    }

    public final void swapItemsWithInventory(Player player, boolean toChar, byte tabId, byte slotId, byte playerBag, byte playerSlotId, int splitedAmount) {
        if ((slotId >= GuildConst.MaxBankSlots && slotId != ItemConst.NullSlot) || tabId >= _GetPurchasedTabsSize()) {
            return;
        }

        BankMoveItemData bankData = new BankMoveItemData(this, player, tabId, slotId);
        PlayerMoveItemData charData = new PlayerMoveItemData(this, player, playerBag, playerSlotId);

        if (toChar) {
            _MoveItems(bankData, charData, splitedAmount);
        } else {
            _MoveItems(charData, bankData, splitedAmount);
        }
    }

    public final void setBankTabText(byte tabId, String text) {
        var pTab = getBankTab(tabId);

        if (pTab != null) {
            pTab.setText(text);
            pTab.sendText(this);

            GuildEventTabTextChanged eventPacket = new GuildEventTabTextChanged();
            eventPacket.tab = tabId;
            broadcastPacket(eventPacket);
        }
    }

    public final void sendBankList(WorldSession session, byte tabId, boolean fullUpdate) {
        var member = getMember(session.getPlayer().getGUID());

        if (member == null) // Shouldn't happen, just in case
        {
            return;
        }

        GuildBankQueryResults packet = new GuildBankQueryResults();

        packet.money = m_bankMoney;
        packet.withdrawalsRemaining = _GetMemberRemainingSlots(member, tabId);
        packet.tab = tabId;
        packet.fullUpdate = fullUpdate;

        // TabInfo
        if (fullUpdate) {
            for (byte i = 0; i < _GetPurchasedTabsSize(); ++i) {
                GuildBankTabInfo tabInfo = new GuildBankTabInfo();
                tabInfo.tabIndex = i;
                tabInfo.name = m_bankTabs.get(i).getName();
                tabInfo.icon = m_bankTabs.get(i).getIcon();
                packet.tabInfo.add(tabInfo);
            }
        }

        if (fullUpdate && _MemberHasTabRights(session.getPlayer().getGUID(), tabId, GuildBankRights.ViewTab)) {
            var tab = getBankTab(tabId);

            if (tab != null) {
                for (byte slotId = 0; slotId < GuildConst.MaxBankSlots; ++slotId) {
                    var tabItem = tab.getItem(slotId);

                    if (tabItem) {
                        GuildBankItemInfo itemInfo = new GuildBankItemInfo();

                        itemInfo.slot = slotId;
                        itemInfo.item.itemID = tabItem.getEntry();
                        itemInfo.count = (int) tabItem.getCount();
                        itemInfo.charges = Math.abs(tabItem.getSpellCharges());
                        itemInfo.enchantmentID = (int) tabItem.getEnchantmentId(EnchantmentSlot.Perm);
                        itemInfo.onUseEnchantmentID = (int) tabItem.getEnchantmentId(EnchantmentSlot.Use);
                        itemInfo.flags = tabItem.getItemData().dynamicFlags;

                        byte i = 0;

                        for (var gemData : tabItem.getItemData().gems) {
                            if (gemData.itemId != 0) {
                                ItemGemData gem = new ItemGemData();
                                gem.slot = i;
                                gem.item = new itemInstance(gemData);
                                itemInfo.socketEnchant.add(gem);
                            }

                            ++i;
                        }

                        itemInfo.locked = false;

                        packet.itemInfo.add(itemInfo);
                    }
                }
            }
        }

        session.sendPacket(packet);
    }

    public final void resetTimes(boolean weekly) {
        for (var member : m_members.values()) {
            member.resetValues(weekly);
            var player = member.findPlayer();

            if (player != null) {
                // tells the client to request bank withdrawal limit
                player.sendPacket(new GuildMemberDailyReset());
            }
        }
    }

    public final void addGuildNews(GuildNews type, ObjectGuid guid, int flags, int value) {
        SQLTransaction trans = new SQLTransaction();
        var news = m_newsLog.addEvent(trans, new NewsLogEntry(m_id, m_newsLog.getNextGUID(), type, guid, flags, value));
        DB.characters.CommitTransaction(trans);

        GuildNewsPkt newsPacket = new GuildNewsPkt();
        news.writePacket(newsPacket);
        broadcastPacket(newsPacket);
    }

    public final void updateCriteria(CriteriaType type, long miscValue1, long miscValue2, long miscValue3, WorldObject refe, Player player) {
        getAchievementMgr().updateCriteria(type, miscValue1, miscValue2, miscValue3, refe, player);
    }

    public final void handleNewsSetSticky(WorldSession session, int newsId, boolean sticky) {
        var newsLog = tangible.ListHelper.find(m_newsLog.getGuildLog(), p -> p.getGUID() == newsId);

        if (newsLog == null) {
            Log.outDebug(LogFilter.guild, "HandleNewsSetSticky: [{0}] requested unknown newsId {1} - Sticky: {2}", session.getPlayerInfo(), newsId, sticky);

            return;
        }

        newsLog.setSticky(sticky);

        GuildNewsPkt newsPacket = new GuildNewsPkt();
        newsLog.writePacket(newsPacket);
        session.sendPacket(newsPacket);
    }


//	public static implicit operator bool(Guild guild)
//		{
//			return guild != null;
//		}

    public final long getId() {
        return m_id;
    }

    public final ObjectGuid getGUID() {
        return ObjectGuid.create(HighGuid.Guild, m_id);
    }

    public final ObjectGuid getLeaderGUID() {
        return m_leaderGuid;
    }

    public final String getName() {
        return m_name;
    }

    public final String getMOTD() {
        return m_motd;
    }

    public final String getInfo() {
        return m_info;
    }

    public final long getCreatedDate() {
        return m_createdDate;
    }

    public final long getBankMoney() {
        return m_bankMoney;
    }

    public final void broadcastWorker(IDoWork<Player> _do) {
        broadcastWorker(_do, null);
    }

    public final void broadcastWorker(IDoWork<Player> _do, Player except) {
        for (var member : m_members.values()) {
            var player = member.findPlayer();

            if (player != null) {
                if (player != except) {
                    _do.invoke(player);
                }
            }
        }
    }

    public final int getMembersCount() {
        return m_members.size();
    }

    public final GuildAchievementMgr getAchievementMgr() {
        return m_achievementSys;
    }

    // Pre-6.x guild leveling
    public final byte getLevel() {
        return GuildConst.OldMaxLevel;
    }

    public final EmblemInfo getEmblemInfo() {
        return m_emblemInfo;
    }

    public final Member getMember(ObjectGuid guid) {
        return m_members.get(guid);
    }

    public final Member getMember(String name) {
        for (var member : m_members.values()) {
            if (Objects.equals(member.getName(), name)) {
                return member;
            }
        }

        return null;
    }

    private void onPlayerStatusChange(Player player, GuildMemberFlags flag, boolean state) {
        var member = getMember(player.getGUID());

        if (member != null) {
            if (state) {
                member.addFlag(flag);
            } else {
                member.removeFlag(flag);
            }
        }
    }

    private void sendEventBankMoneyChanged() {
        GuildEventBankMoneyChanged eventPacket = new GuildEventBankMoneyChanged();
        eventPacket.money = getBankMoney();
        broadcastPacket(eventPacket);
    }

    private void sendEventMOTD(WorldSession session) {
        sendEventMOTD(session, false);
    }

    private void sendEventMOTD(WorldSession session, boolean broadcast) {
        GuildEventMotd eventPacket = new GuildEventMotd();
        eventPacket.motdText = getMOTD();

        if (broadcast) {
            broadcastPacket(eventPacket);
        } else {
            session.sendPacket(eventPacket);
            Log.outDebug(LogFilter.guild, "SMSG_GUILD_EVENT_MOTD [{0}] ", session.getPlayerInfo());
        }
    }

    private void sendEventNewLeader(Member newLeader, Member oldLeader) {
        sendEventNewLeader(newLeader, oldLeader, false);
    }

    private void sendEventNewLeader(Member newLeader, Member oldLeader, boolean isSelfPromoted) {
        GuildEventNewLeader eventPacket = new GuildEventNewLeader();
        eventPacket.selfPromoted = isSelfPromoted;

        if (newLeader != null) {
            eventPacket.newLeaderGUID = newLeader.getGUID();
            eventPacket.newLeaderName = newLeader.getName();
            eventPacket.newLeaderVirtualRealmAddress = global.getWorldMgr().getVirtualRealmAddress();
        }

        if (oldLeader != null) {
            eventPacket.oldLeaderGUID = oldLeader.getGUID();
            eventPacket.oldLeaderName = oldLeader.getName();
            eventPacket.oldLeaderVirtualRealmAddress = global.getWorldMgr().getVirtualRealmAddress();
        }

        broadcastPacket(eventPacket);
    }

    private void sendEventPlayerLeft(Player leaver, Player remover) {
        sendEventPlayerLeft(leaver, remover, false);
    }

    private void sendEventPlayerLeft(Player leaver) {
        sendEventPlayerLeft(leaver, null, false);
    }

    private void sendEventPlayerLeft(Player leaver, Player remover, boolean isRemoved) {
        GuildEventPlayerLeft eventPacket = new GuildEventPlayerLeft();
        eventPacket.removed = isRemoved;
        eventPacket.leaverGUID = leaver.getGUID();
        eventPacket.leaverName = leaver.getName();
        eventPacket.leaverVirtualRealmAddress = global.getWorldMgr().getVirtualRealmAddress();

        if (isRemoved && remover) {
            eventPacket.removerGUID = remover.getGUID();
            eventPacket.removerName = remover.getName();
            eventPacket.removerVirtualRealmAddress = global.getWorldMgr().getVirtualRealmAddress();
        }

        broadcastPacket(eventPacket);
    }

    private void sendEventPresenceChanged(WorldSession session, boolean loggedOn) {
        sendEventPresenceChanged(session, loggedOn, false);
    }

    private void sendEventPresenceChanged(WorldSession session, boolean loggedOn, boolean broadcast) {
        var player = session.getPlayer();

        GuildEventPresenceChange eventPacket = new GuildEventPresenceChange();
        eventPacket.guid = player.getGUID();
        eventPacket.name = player.getName();
        eventPacket.virtualRealmAddress = global.getWorldMgr().getVirtualRealmAddress();
        eventPacket.loggedOn = loggedOn;
        eventPacket.mobile = false;

        if (broadcast) {
            broadcastPacket(eventPacket);
        } else {
            session.sendPacket(eventPacket);
        }
    }

    private RankInfo getRankInfo(GuildRankId rankId) {
        return tangible.ListHelper.find(m_ranks, rank -> rank.getId() == rankId);
    }

    private RankInfo getRankInfo(GuildRankOrder rankOrder) {
        return tangible.ListHelper.find(m_ranks, rank -> rank.getOrder() == rankOrder);
    }

    // Private methods
    private void _CreateNewBankTab() {
        var tabId = _GetPurchasedTabsSize(); // Next free id
        m_bankTabs.add(new bankTab(m_id, tabId));

        SQLTransaction trans = new SQLTransaction();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GUILD_BANK_TAB);
        stmt.AddValue(0, m_id);
        stmt.AddValue(1, tabId);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.INS_GUILD_BANK_TAB);
        stmt.AddValue(0, m_id);
        stmt.AddValue(1, tabId);
        trans.append(stmt);

        ++tabId;

        for (var rank : m_ranks) {
            rank.createMissingTabsIfNeeded(tabId, trans, false);
        }

        DB.characters.CommitTransaction(trans);
    }

    private void _CreateDefaultGuildRanks(SQLTransaction trans) {
        _CreateDefaultGuildRanks(trans, locale.enUS);
    }

    private void _CreateDefaultGuildRanks(SQLTransaction trans, Locale loc) {
        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GUILD_RANKS);
        stmt.AddValue(0, m_id);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GUILD_BANK_RIGHTS);
        stmt.AddValue(0, m_id);
        trans.append(stmt);

        _CreateRank(trans, global.getObjectMgr().getSysMessage(SysMessage.GuildMaster, loc), GuildRankRights.All);
        _CreateRank(trans, global.getObjectMgr().getSysMessage(SysMessage.GuildOfficer, loc), GuildRankRights.All);
        _CreateRank(trans, global.getObjectMgr().getSysMessage(SysMessage.GuildVeteran, loc), GuildRankRights.GChatListen.getValue() | GuildRankRights.GChatSpeak.getValue());
        _CreateRank(trans, global.getObjectMgr().getSysMessage(SysMessage.GuildMember, loc), GuildRankRights.GChatListen.getValue() | GuildRankRights.GChatSpeak.getValue());
        _CreateRank(trans, global.getObjectMgr().getSysMessage(SysMessage.GuildInitiate, loc), GuildRankRights.GChatListen.getValue() | GuildRankRights.GChatSpeak.getValue());
    }

    private boolean _CreateRank(SQLTransaction trans, String name, GuildRankRights rights) {
        if (m_ranks.size() >= GuildConst.MaxRanks) {
            return false;
        }

        byte newRankId = 0;

        while (getRankInfo(GuildRankId.forValue(newRankId)) != null) {
            ++newRankId;
        }

        // Ranks represent sequence 0, 1, 2, ... where 0 means guildmaster
        RankInfo info = new RankInfo(m_id, GuildRankId.forValue(newRankId), GuildRankOrder.forValue(m_ranks.size()), name, rights, 0);
        m_ranks.add(info);

        var isInTransaction = trans != null;

        if (!isInTransaction) {
            trans = new SQLTransaction();
        }

        info.createMissingTabsIfNeeded(_GetPurchasedTabsSize(), trans);
        info.saveToDB(trans);
        DB.characters.CommitTransaction(trans);

        if (!isInTransaction) {
            DB.characters.CommitTransaction(trans);
        }

        return true;
    }

    private void _UpdateAccountsNumber() {
        // We use a set to be sure each element will be unique
        ArrayList<Integer> accountsIdSet = new ArrayList<>();

        for (var member : m_members.values()) {
            accountsIdSet.add(member.getAccountId());
        }

        m_accountsNumber = (int) accountsIdSet.size();
    }

    private boolean _IsLeader(Player player) {
        if (Objects.equals(player.getGUID(), m_leaderGuid)) {
            return true;
        }

        var member = getMember(player.getGUID());

        if (member != null) {
            return member.isRank(GuildRankId.GuildMaster);
        }

        return false;
    }

    private void _DeleteBankItems(SQLTransaction trans, boolean removeItemsFromDB) {
        for (byte tabId = 0; tabId < _GetPurchasedTabsSize(); ++tabId) {
            m_bankTabs.get(tabId).delete(trans, removeItemsFromDB);
            m_bankTabs.set(tabId, null);
        }

        m_bankTabs.clear();
    }

    private boolean _ModifyBankMoney(SQLTransaction trans, long amount, boolean add) {
        if (add) {
            m_bankMoney += amount;
        } else {
            // Check if there is enough money in bank.
            if (m_bankMoney < amount) {
                return false;
            }

            m_bankMoney -= amount;
        }

        var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GUILD_BANK_MONEY);
        stmt.AddValue(0, m_bankMoney);
        stmt.AddValue(1, m_id);
        trans.append(stmt);

        return true;
    }

    private void _SetLeader(SQLTransaction trans, Member leader) {
        var isInTransaction = trans != null;

        if (!isInTransaction) {
            trans = new SQLTransaction();
        }

        m_leaderGuid = leader.getGUID();
        leader.changeRank(trans, GuildRankId.GuildMaster);

        var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GUILD_LEADER);
        stmt.AddValue(0, m_leaderGuid.getCounter());
        stmt.AddValue(1, m_id);
        trans.append(stmt);

        if (!isInTransaction) {
            DB.characters.CommitTransaction(trans);
        }
    }

    private void _SetRankBankMoneyPerDay(GuildRankId rankId, int moneyPerDay) {
        var rankInfo = getRankInfo(rankId);

        if (rankInfo != null) {
            rankInfo.setBankMoneyPerDay(moneyPerDay);
        }
    }

    private void _SetRankBankTabRightsAndSlots(GuildRankId rankId, GuildBankRightsAndSlots rightsAndSlots) {
        _SetRankBankTabRightsAndSlots(rankId, rightsAndSlots, true);
    }

    private void _SetRankBankTabRightsAndSlots(GuildRankId rankId, GuildBankRightsAndSlots rightsAndSlots, boolean saveToDB) {
        if (rightsAndSlots.getTabId() >= _GetPurchasedTabsSize()) {
            return;
        }

        var rankInfo = getRankInfo(rankId);

        if (rankInfo != null) {
            rankInfo.setBankTabSlotsAndRights(rightsAndSlots, saveToDB);
        }
    }

    private String _GetRankName(GuildRankId rankId) {
        var rankInfo = getRankInfo(rankId);

        if (rankInfo != null) {
            return rankInfo.getName();
        }

        return "<unknown>";
    }

    private GuildRankRights _GetRankRights(GuildRankId rankId) {
        var rankInfo = getRankInfo(rankId);

        if (rankInfo != null) {
            return rankInfo.getRights();
        }

        return 0;
    }

    private int _GetRankBankMoneyPerDay(GuildRankId rankId) {
        var rankInfo = getRankInfo(rankId);

        if (rankInfo != null) {
            return rankInfo.getBankMoneyPerDay();
        }

        return 0;
    }

    private int _GetRankBankTabSlotsPerDay(GuildRankId rankId, byte tabId) {
        if (tabId < _GetPurchasedTabsSize()) {
            var rankInfo = getRankInfo(rankId);

            if (rankInfo != null) {
                return rankInfo.getBankTabSlotsPerDay(tabId);
            }
        }

        return 0;
    }

    private GuildBankRights _GetRankBankTabRights(GuildRankId rankId, byte tabId) {
        var rankInfo = getRankInfo(rankId);

        if (rankInfo != null) {
            return rankInfo.getBankTabRights(tabId);
        }

        return 0;
    }

    private int _GetMemberRemainingSlots(Member member, byte tabId) {
        var rankId = member.getRankId();

        if (rankId == GuildRankId.GuildMaster) {
            return GuildConst.WithdrawSlotUnlimited;
        }

        if ((_GetRankBankTabRights(rankId, tabId).getValue() & GuildBankRights.ViewTab.getValue()) != 0) {
            var remaining = _GetRankBankTabSlotsPerDay(rankId, tabId) - (int) member.getBankTabWithdrawValue(tabId);

            if (remaining > 0) {
                return remaining;
            }
        }

        return 0;
    }

    private long _GetMemberRemainingMoney(Member member) {
        var rankId = member.getRankId();

        if (rankId == GuildRankId.GuildMaster) {
            return Long.MAX_VALUE;
        }

        if ((_GetRankRights(rankId) & (GuildRankRights.WithdrawRepair.getValue() | GuildRankRights.WithdrawGold.getValue())) != 0) {
            var remaining = (long) ((_GetRankBankMoneyPerDay(rankId) * MoneyConstants.gold) - member.getBankMoneyWithdrawValue());

            if (remaining > 0) {
                return remaining;
            }
        }

        return 0;
    }

    private void _UpdateMemberWithdrawSlots(SQLTransaction trans, ObjectGuid guid, byte tabId) {
        var member = getMember(guid);

        if (member != null) {
            member.updateBankTabWithdrawValue(trans, tabId, 1);
        }
    }

    private boolean _MemberHasTabRights(ObjectGuid guid, byte tabId, GuildBankRights rights) {
        var member = getMember(guid);

        if (member != null) {
            // Leader always has full rights
            if (member.isRank(GuildRankId.GuildMaster) || Objects.equals(m_leaderGuid, guid)) {
                return true;
            }

            return (_GetRankBankTabRights(member.getRankId(), tabId).getValue() & rights.getValue()) == rights.getValue();
        }

        return false;
    }

    private void _LogEvent(GuildEventLogTypes eventType, long playerGuid1, long playerGuid2) {
        _LogEvent(eventType, playerGuid1, playerGuid2, 0);
    }

    private void _LogEvent(GuildEventLogTypes eventType, long playerGuid1) {
        _LogEvent(eventType, playerGuid1, 0, 0);
    }

    private void _LogEvent(GuildEventLogTypes eventType, long playerGuid1, long playerGuid2, byte newRank) {
        SQLTransaction trans = new SQLTransaction();
        m_eventLog.addEvent(trans, new EventLogEntry(m_id, m_eventLog.getNextGUID(), eventType, playerGuid1, playerGuid2, newRank));
        DB.characters.CommitTransaction(trans);

        global.getScriptMgr().<IGuildOnEvent>ForEach(p -> p.OnEvent(this, (byte) eventType.getValue(), playerGuid1, playerGuid2, newRank));
    }

    private void _LogBankEvent(SQLTransaction trans, GuildBankEventLogTypes eventType, byte tabId, long lowguid, int itemOrMoney, short itemStackCount) {
        _LogBankEvent(trans, eventType, tabId, lowguid, itemOrMoney, itemStackCount, 0);
    }

    private void _LogBankEvent(SQLTransaction trans, GuildBankEventLogTypes eventType, byte tabId, long lowguid, int itemOrMoney) {
        _LogBankEvent(trans, eventType, tabId, lowguid, itemOrMoney, 0, 0);
    }

    private void _LogBankEvent(SQLTransaction trans, GuildBankEventLogTypes eventType, byte tabId, long lowguid, int itemOrMoney, short itemStackCount, byte destTabId) {
        if (tabId > GuildConst.MaxBankTabs) {
            return;
        }

        // not logging moves within the same tab
        if (eventType == GuildBankEventLogTypes.MoveItem && tabId == destTabId) {
            return;
        }

        var dbTabId = tabId;

        if (BankEventLogEntry.isMoneyEvent(eventType)) {
            tabId = GuildConst.MaxBankTabs;
            dbTabId = GuildConst.BankMoneyLogsTab;
        }

        var pLog = m_bankEventLog[tabId];
        pLog.addEvent(trans, new BankEventLogEntry(m_id, pLog.getNextGUID(), eventType, dbTabId, lowguid, itemOrMoney, itemStackCount, destTabId));

        global.getScriptMgr().<IGuildOnBankEvent>ForEach(p -> p.OnBankEvent(this, (byte) eventType.getValue(), tabId, lowguid, itemOrMoney, itemStackCount, destTabId));
    }

    private Item _GetItem(byte tabId, byte slotId) {
        var tab = getBankTab(tabId);

        if (tab != null) {
            return tab.getItem(slotId);
        }

        return null;
    }


    ///#region Fields

    private void _RemoveItem(SQLTransaction trans, byte tabId, byte slotId) {
        var pTab = getBankTab(tabId);

        if (pTab != null) {
            pTab.setItem(trans, slotId, null);
        }
    }

    private void _MoveItems(MoveItemData pSrc, MoveItemData pDest, int splitedAmount) {
        // 1. Initialize source item
        if (!pSrc.initItem()) {
            return; // No source item
        }

        // 2. Check source item
        tangible.RefObject<Integer> tempRef_splitedAmount = new tangible.RefObject<Integer>(splitedAmount);
        if (!pSrc.checkItem(tempRef_splitedAmount)) {
            splitedAmount = tempRef_splitedAmount.refArgValue;
            return; // Source item or splited amount is invalid
        } else {
            splitedAmount = tempRef_splitedAmount.refArgValue;
        }

        // 3. Check destination rights
        if (!pDest.hasStoreRights(pSrc)) {
            return; // Player has no rights to store item in destination
        }

        // 4. Check source withdraw rights
        if (!pSrc.hasWithdrawRights(pDest)) {
            return; // Player has no rights to withdraw items from source
        }

        // 5. Check split
        if (splitedAmount != 0) {
            // 5.1. Clone source item
            if (!pSrc.cloneItem(splitedAmount)) {
                return; // Item could not be cloned
            }

            // 5.2. Move splited item to destination
            _DoItemsMove(pSrc, pDest, true, splitedAmount);
        } else // 6. No split
        {
            // 6.1. Try to merge items in destination (pDest.getItem() == NULL)
            var mergeAttemptResult = _DoItemsMove(pSrc, pDest, false);

            if (mergeAttemptResult != InventoryResult.Ok) // Item could not be merged
            {
                // 6.2. Try to swap items
                // 6.2.1. Initialize destination item
                if (!pDest.initItem()) {
                    pSrc.sendEquipError(mergeAttemptResult, pSrc.getItem(false));

                    return;
                }

                // 6.2.2. Check rights to store item in source (opposite direction)
                if (!pSrc.hasStoreRights(pDest)) {
                    return; // Player has no rights to store item in source (opposite direction)
                }

                if (!pDest.hasWithdrawRights(pSrc)) {
                    return; // Player has no rights to withdraw item from destination (opposite direction)
                }

                // 6.2.3. Swap items (pDest.getItem() != NULL)
                _DoItemsMove(pSrc, pDest, true);
            }
        }

        // 7. Send changes
        _SendBankContentUpdate(pSrc, pDest);
    }

    private InventoryResult _DoItemsMove(MoveItemData pSrc, MoveItemData pDest, boolean sendError) {
        return _DoItemsMove(pSrc, pDest, sendError, 0);
    }

    private InventoryResult _DoItemsMove(MoveItemData pSrc, MoveItemData pDest, boolean sendError, int splitedAmount) {
        var pDestItem = pDest.getItem();
        var swap = (pDestItem != null);

        var pSrcItem = pSrc.getItem(splitedAmount != 0);
        // 1. Can store source item in destination
        var destResult = pDest.canStore(pSrcItem, swap, sendError);

        if (destResult != InventoryResult.Ok) {
            return destResult;
        }

        // 2. Can store destination item in source
        if (swap) {
            var srcResult = pSrc.canStore(pDestItem, true, true);

            if (srcResult != InventoryResult.Ok) {
                return srcResult;
            }
        }

        // GM LOG (@todo move to scripts)
        pDest.logAction(pSrc);

        if (swap) {
            pSrc.logAction(pDest);
        }

        SQLTransaction trans = new SQLTransaction();
        // 3. Log bank events
        pDest.logBankEvent(trans, pSrc, pSrcItem.getCount());

        if (swap) {
            pSrc.logBankEvent(trans, pDest, pDestItem.getCount());
        }

        // 4. Remove item from source
        pSrc.removeItem(trans, pDest, splitedAmount);

        // 5. Remove item from destination
        if (swap) {
            pDest.removeItem(trans, pSrc);
        }

        // 6. Store item in destination
        pDest.storeItem(trans, pSrcItem);

        // 7. Store item in source
        if (swap) {
            pSrc.storeItem(trans, pDestItem);
        }

        DB.characters.CommitTransaction(trans);

        return InventoryResult.Ok;
    }

    private void _SendBankContentUpdate(MoveItemData pSrc, MoveItemData pDest) {
        byte tabId = 0;
        ArrayList<Byte> slots = new ArrayList<>();

        if (pSrc.isBank()) // B .
        {
            tabId = pSrc.getContainer();
            slots.add(0, pSrc.getSlotId());

            if (pDest.isBank()) // B . B
            {
                // Same tab - add destination slots to collection
                if (pDest.getContainer() == pSrc.getContainer()) {
                    pDest.copySlots(slots);
                } else // Different tabs - send second message
                {
                    ArrayList<Byte> destSlots = new ArrayList<>();
                    pDest.copySlots(destSlots);
                    _SendBankContentUpdate(pDest.getContainer(), destSlots);
                }
            }
        } else if (pDest.isBank()) // C . B
        {
            tabId = pDest.getContainer();
            pDest.copySlots(slots);
        }

        _SendBankContentUpdate(tabId, slots);
    }

    private void _SendBankContentUpdate(byte tabId, ArrayList<Byte> slots) {
        var tab = getBankTab(tabId);

        if (tab != null) {
            GuildBankQueryResults packet = new GuildBankQueryResults();
            packet.fullUpdate = true; // @todo
            packet.tab = tabId;
            packet.money = m_bankMoney;

            for (var slot : slots) {
                var tabItem = tab.getItem(slot);

                GuildBankItemInfo itemInfo = new GuildBankItemInfo();

                itemInfo.slot = slot;
                itemInfo.item.itemID = tabItem ? tabItem.getEntry() : 0;
                itemInfo.count = (int) (tabItem ? tabItem.getCount() : 0);
                itemInfo.enchantmentID = (int) (tabItem ? tabItem.getEnchantmentId(EnchantmentSlot.Perm) : 0);
                itemInfo.charges = tabItem ? Math.abs(tabItem.getSpellCharges()) : 0;
                itemInfo.onUseEnchantmentID = (int) (tabItem ? tabItem.getEnchantmentId(EnchantmentSlot.Use) : 0);
                itemInfo.flags = 0;
                itemInfo.locked = false;

                if (tabItem != null) {
                    byte i = 0;

                    for (var gemData : tabItem.getItemData().gems) {
                        if (gemData.itemId != 0) {
                            ItemGemData gem = new ItemGemData();
                            gem.slot = i;
                            gem.item = new itemInstance(gemData);
                            itemInfo.socketEnchant.add(gem);
                        }

                        ++i;
                    }
                }

                packet.itemInfo.add(itemInfo);
            }


            for (var(guid, member) : m_members) {
                if (!_MemberHasTabRights(guid, tabId, GuildBankRights.ViewTab)) {
                    continue;
                }

                var player = member.findPlayer();

                if (player == null) {
                    continue;
                }

                packet.withdrawalsRemaining = _GetMemberRemainingSlots(member, tabId);
                player.sendPacket(packet);
            }
        }
    }

    private void sendGuildRanksUpdate(ObjectGuid setterGuid, ObjectGuid targetGuid, GuildRankId rank) {
        var member = getMember(targetGuid);

        GuildSendRankChange rankChange = new GuildSendRankChange();
        rankChange.officer = setterGuid;
        rankChange.other = targetGuid;
        rankChange.rankID = (byte) rank.getValue();
        rankChange.promote = (rank.getValue() < member.getRankId());
        broadcastPacket(rankChange);

        member.changeRank(null, rank);

        Log.outDebug(LogFilter.Network, "SMSG_GUILD_RANKS_UPDATE [Broadcast] Target: {0}, Issuer: {1}, RankId: {2}", targetGuid.toString(), setterGuid.toString(), rank);
    }

    private boolean hasAchieved(int achievementId) {
        return getAchievementMgr().hasAchieved(achievementId);
    }

    private byte _GetRanksSize() {
        return (byte) m_ranks.size();
    }

    private RankInfo getRankInfo(int rankId) {
        return rankId < _GetRanksSize() ? m_ranks.get((int) rankId) : null;
    }

    private boolean _HasRankRight(Player player, GuildRankRights right) {
        if (player != null) {
            var member = getMember(player.getGUID());

            if (member != null) {
                return (_GetRankRights(member.getRankId()).getValue() & right.getValue()) != GuildRankRights.NONE.getValue();
            }

            return false;
        }

        return false;
    }

    private GuildRankId _GetLowestRankId() {
        return m_ranks.get(m_ranks.size() - 1).getId();
    }

    private byte _GetPurchasedTabsSize() {
        return (byte) m_bankTabs.size();
    }

    private BankTab getBankTab(byte tabId) {
        return tabId < m_bankTabs.size() ? m_bankTabs.get(tabId) : null;
    }

    private void _DeleteMemberFromDB(SQLTransaction trans, long lowguid) {
        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GUILD_MEMBER);
        stmt.AddValue(0, lowguid);
        DB.characters.ExecuteOrAppend(trans, stmt);
    }

    private long getGuildBankTabPrice(byte tabId) {
        // these prices are in gold units, not copper
        switch (tabId) {
            case 0:
                return 100;
            case 1:
                return 250;
            case 2:
                return 500;
            case 3:
                return 1000;
            case 4:
                return 2500;
            case 5:
                return 5000;
            default:
                return 0;
        }
    }


    ///#endregion


    ///#region Classes

    public static class Member {
        private final long m_guildId;
        private final int[] m_bankWithdraw = new int[GuildConst.MaxBankTabs];
        private ObjectGuid m_guid = ObjectGuid.EMPTY;
        private String m_name;
        private int m_zoneId;
        private byte m_level;
        private Race m_race = race.values()[0];
        private PlayerClass m_class = UnitClass.values()[0];
        private GuildMemberFlags m_flags = GuildMemberFlags.values()[0];
        private long m_logoutTime;
        private int m_accountId;
        private GuildRankId m_rankId = GuildRankId.values()[0];
        private String m_publicNote = "";
        private String m_officerNote = "";
        private ArrayList<Integer> m_trackedCriteriaIds = new ArrayList<>();
        private long m_bankWithdrawMoney;
        private int m_achievementPoints;
        private long m_totalActivity;
        private long m_weekActivity;
        private int m_totalReputation;
        private int m_weekReputation;

        public member(long guildId, ObjectGuid guid, GuildRankId rankId) {
            m_guildId = guildId;
            m_guid = guid;
            m_zoneId = 0;
            m_level = 0;
            m_class = UnitClass.forValue(0);
            m_flags = GuildMemberFlags.NONE;
            m_logoutTime = (long) GameTime.getGameTime();
            m_accountId = 0;
            m_rankId = rankId;
            m_achievementPoints = 0;
            m_totalActivity = 0;
            m_weekActivity = 0;
            m_totalReputation = 0;
            m_weekReputation = 0;
        }

        public final void setStats(Player player) {
            m_name = player.getName();
            m_level = (byte) player.getLevel();
            m_race = player.getRace();
            m_class = player.getUnitClass();
            gender = player.getNativeGender();
            m_zoneId = player.getZoneId();
            m_accountId = player.getSession().getAccountId();
            m_achievementPoints = player.getAchievementPoints();
        }

        public final void setStats(String name, byte level, Race race, PlayerClass _class, Gender gender, int zoneId, int accountId, int reputation) {
            m_name = name;
            m_level = level;
            m_race = race;
            m_class = _class;
            gender = gender;
            m_zoneId = zoneId;
            m_accountId = accountId;
            m_totalReputation = reputation;
        }

        public final void changeRank(SQLTransaction trans, GuildRankId newRank) {
            m_rankId = newRank;

            // Update rank information in player's field, if he is online.
            var player = findConnectedPlayer();

            if (player != null) {
                player.setGuildRank((byte) newRank.getValue());
            }

            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GUILD_MEMBER_RANK);
            stmt.AddValue(0, (byte) newRank.getValue());
            stmt.AddValue(1, m_guid.getCounter());
            DB.characters.ExecuteOrAppend(trans, stmt);
        }

        public final void saveToDB(SQLTransaction trans) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_GUILD_MEMBER);
            stmt.AddValue(0, m_guildId);
            stmt.AddValue(1, m_guid.getCounter());
            stmt.AddValue(2, (byte) m_rankId.getValue());
            stmt.AddValue(3, m_publicNote);
            stmt.AddValue(4, m_officerNote);
            DB.characters.ExecuteOrAppend(trans, stmt);
        }

        public final boolean loadFromDB(SQLFields field) {
            m_publicNote = field.<String>Read(3);
            m_officerNote = field.<String>Read(4);

            for (byte i = 0; i < GuildConst.MaxBankTabs; ++i) {
                m_bankWithdraw[i] = field.<Integer>Read(5 + i);
            }

            m_bankWithdrawMoney = field.<Long>Read(13);

            setStats(field.<String>Read(14), field.<Byte>Read(15), race.forValue(field.<Byte>Read(16)), UnitClass.forValue(field.<Byte>Read(17)), gender.forValue((byte) field.<Byte>Read(18)), field.<SHORT>Read(19), field.<Integer>Read(20), 0);

            m_logoutTime = field.<Long>Read(21); // character.logout_time
            m_totalActivity = 0;
            m_weekActivity = 0;
            m_weekReputation = 0;

            if (!checkStats()) {
                return false;
            }

            if (m_zoneId == 0) {
                Log.outError(LogFilter.guild, "Player ({0}) has broken zone-data", m_guid.toString());
                m_zoneId = player.getZoneIdFromDB(m_guid);
            }

            resetFlags();

            return true;
        }

        public final boolean checkStats() {
            if (m_level < 1) {
                Log.outError(LogFilter.guild, String.format("%1$s has a broken data in field `character`.`level`, deleting him from guild!", m_guid));

                return false;
            }

            if (!CliDB.ChrRacesStorage.containsKey((int) m_race.getValue())) {
                Log.outError(LogFilter.guild, String.format("%1$s has a broken data in field `character`.`race`, deleting him from guild!", m_guid));

                return false;
            }

            if (!CliDB.ChrClassesStorage.containsKey((int) m_class.getValue())) {
                Log.outError(LogFilter.guild, String.format("%1$s has a broken data in field `character`.`class`, deleting him from guild!", m_guid));

                return false;
            }

            return true;
        }

        public final float getInactiveDays() {
            if (isOnline()) {
                return 0.0f;
            }

            return (float) ((GameTime.getGameTime() - (long) getLogoutTime()) / (float) time.Day);
        }

        // Decreases amount of slots left for today.
        public final void updateBankTabWithdrawValue(SQLTransaction trans, byte tabId, int amount) {
            m_bankWithdraw[tabId] += amount;

            var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_GUILD_MEMBER_WITHDRAW_TABS);
            stmt.AddValue(0, m_guid.getCounter());

            for (byte i = 0; i < GuildConst.MaxBankTabs; ) {
                var withdraw = m_bankWithdraw[i++];
                stmt.AddValue(i, withdraw);
            }

            DB.characters.ExecuteOrAppend(trans, stmt);
        }

        // Decreases amount of money left for today.
        public final void updateBankMoneyWithdrawValue(SQLTransaction trans, long amount) {
            m_bankWithdrawMoney += amount;

            var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_GUILD_MEMBER_WITHDRAW_MONEY);
            stmt.AddValue(0, m_guid.getCounter());
            stmt.AddValue(1, m_bankWithdrawMoney);
            DB.characters.ExecuteOrAppend(trans, stmt);
        }

        public final void resetValues() {
            resetValues(false);
        }

        public final void resetValues(boolean weekly) {
            for (byte tabId = 0; tabId < GuildConst.MaxBankTabs; ++tabId) {
                m_bankWithdraw[tabId] = 0;
            }

            m_bankWithdrawMoney = 0;

            if (weekly) {
                m_weekActivity = 0;
                m_weekReputation = 0;
            }
        }

        public final void addFlag(GuildMemberFlags var) {
            m_flags = GuildMemberFlags.forValue(m_flags.getValue() | var.getValue());
        }

        public final void removeFlag(GuildMemberFlags var) {
            m_flags = GuildMemberFlags.forValue(m_flags.getValue() & ~var.getValue());
        }

        public final void resetFlags() {
            m_flags = GuildMemberFlags.NONE;
        }

        public final ObjectGuid getGUID() {
            return m_guid;
        }

        public final String getName() {
            return m_name;
        }

        public final int getAccountId() {
            return m_accountId;
        }

        public final GuildRankId getRankId() {
            return m_rankId;
        }

        public final long getLogoutTime() {
            return m_logoutTime;
        }

        public final String getPublicNote() {
            return m_publicNote;
        }

        public final void setPublicNote(String publicNote) {
            if (Objects.equals(m_publicNote, publicNote)) {
                return;
            }

            m_publicNote = publicNote;

            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GUILD_MEMBER_PNOTE);
            stmt.AddValue(0, publicNote);
            stmt.AddValue(1, m_guid.getCounter());
            DB.characters.execute(stmt);
        }

        public final String getOfficerNote() {
            return m_officerNote;
        }

        public final void setOfficerNote(String officerNote) {
            if (Objects.equals(m_officerNote, officerNote)) {
                return;
            }

            m_officerNote = officerNote;

            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GUILD_MEMBER_OFFNOTE);
            stmt.AddValue(0, officerNote);
            stmt.AddValue(1, m_guid.getCounter());
            DB.characters.execute(stmt);
        }

        public final Race getRace() {
            return m_race;
        }

        public final PlayerClass getClass() {
            return m_class;
        }

        public final Gender getGender() {
            return gender;
        }

        public final byte getLevel() {
            return m_level;
        }


        ///#region Fields

        public final void setLevel(int var) {
            m_level = (byte) var;
        }

        public final GuildMemberFlags getFlags() {
            return m_flags;
        }

        public final int getZoneId() {
            return m_zoneId;
        }

        public final void setZoneId(int id) {
            m_zoneId = id;
        }

        public final int getAchievementPoints() {
            return m_achievementPoints;
        }

        public final void setAchievementPoints(int val) {
            m_achievementPoints = val;
        }

        public final long getTotalActivity() {
            return m_totalActivity;
        }

        public final long getWeekActivity() {
            return m_weekActivity;
        }        private Gender gender = gender.values()[0];

        public final int getTotalReputation() {
            return m_totalReputation;
        }

        public final int getWeekReputation() {
            return m_weekReputation;
        }

        public final ArrayList<Integer> getTrackedCriteriaIds() {
            return m_trackedCriteriaIds;
        }

        public final void setTrackedCriteriaIds(ArrayList<Integer> criteriaIds) {
            m_trackedCriteriaIds = criteriaIds;
        }

        public final boolean isTrackingCriteriaId(int criteriaId) {
            return m_trackedCriteriaIds.contains(criteriaId);
        }

        public final boolean isOnline() {
            return m_flags.hasFlag(GuildMemberFlags.online);
        }

        public final void updateLogoutTime() {
            m_logoutTime = (long) GameTime.getGameTime();
        }

        public final boolean isRank(GuildRankId rankId) {
            return m_rankId == rankId;
        }

        public final boolean isSamePlayer(ObjectGuid guid) {
            return Objects.equals(m_guid, guid);
        }

        public final int getBankTabWithdrawValue(byte tabId) {
            return m_bankWithdraw[tabId];
        }

        public final long getBankMoneyWithdrawValue() {
            return m_bankWithdrawMoney;
        }

        public final Player findPlayer() {
            return global.getObjAccessor().findPlayer(m_guid);
        }

        private Player findConnectedPlayer() {
            return global.getObjAccessor().findConnectedPlayer(m_guid);
        }



        ///#endregion
    }

    public static class LogEntry {
        public long m_guildId;
        public int m_guid;
        public long m_timestamp;

        public LogEntry(long guildId, int guid) {
            m_guildId = guildId;
            m_guid = guid;
            m_timestamp = GameTime.getGameTime();
        }

        public LogEntry(long guildId, int guid, long timestamp) {
            m_guildId = guildId;
            m_guid = guid;
            m_timestamp = timestamp;
        }

        public final int getGUID() {
            return m_guid;
        }

        public final long getTimestamp() {
            return m_timestamp;
        }

        public void saveToDB(SQLTransaction trans) {
        }
    }

    public static class EventLogEntry extends LogEntry {
        private final GuildEventLogTypes m_eventType;
        private final long m_playerGuid1;
        private final long m_playerGuid2;
        private final byte m_newRank;

        public EventLogEntry(long guildId, int guid, GuildEventLogTypes eventType, long playerGuid1, long playerGuid2, byte newRank) {
            super(guildId, guid);
            m_eventType = eventType;
            m_playerGuid1 = playerGuid1;
            m_playerGuid2 = playerGuid2;
            m_newRank = newRank;
        }

        public EventLogEntry(long guildId, int guid, long timestamp, GuildEventLogTypes eventType, long playerGuid1, long playerGuid2, byte newRank) {
            super(guildId, guid, timestamp);
            m_eventType = eventType;
            m_playerGuid1 = playerGuid1;
            m_playerGuid2 = playerGuid2;
            m_newRank = newRank;
        }

        @Override
        public void saveToDB(SQLTransaction trans) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GUILD_EVENTLOG);
            stmt.AddValue(0, m_guildId);
            stmt.AddValue(1, m_guid);
            trans.append(stmt);

            byte index = 0;
            stmt = DB.characters.GetPreparedStatement(CharStatements.INS_GUILD_EVENTLOG);
            stmt.AddValue(index, m_guildId);
            stmt.AddValue(++index, m_guid);
            stmt.AddValue(++index, (byte) m_eventType.getValue());
            stmt.AddValue(++index, m_playerGuid1);
            stmt.AddValue(++index, m_playerGuid2);
            stmt.AddValue(++index, m_newRank);
            stmt.AddValue(++index, m_timestamp);
            trans.append(stmt);
        }

        public final void writePacket(GuildEventLogQueryResults packet) {
            var playerGUID = ObjectGuid.create(HighGuid.Player, m_playerGuid1);
            var otherGUID = ObjectGuid.create(HighGuid.Player, m_playerGuid2);

            GuildEventEntry eventEntry = new GuildEventEntry();
            eventEntry.playerGUID = playerGUID;
            eventEntry.otherGUID = otherGUID;
            eventEntry.transactionType = (byte) m_eventType.getValue();
            eventEntry.transactionDate = (int) (GameTime.getGameTime() - m_timestamp);
            eventEntry.rankID = m_newRank;
            packet.entry.add(eventEntry);
        }
    }

    public static class BankEventLogEntry extends LogEntry {
        private final GuildBankEventLogTypes m_eventType;
        private final byte m_bankTabId;
        private final long m_playerGuid;
        private final long m_itemOrMoney;
        private final short m_itemStackCount;
        private final byte m_destTabId;

        public BankEventLogEntry(long guildId, int guid, GuildBankEventLogTypes eventType, byte tabId, long playerGuid, long itemOrMoney, short itemStackCount, byte destTabId) {
            super(guildId, guid);
            m_eventType = eventType;
            m_bankTabId = tabId;
            m_playerGuid = playerGuid;
            m_itemOrMoney = itemOrMoney;
            m_itemStackCount = itemStackCount;
            m_destTabId = destTabId;
        }

        public BankEventLogEntry(long guildId, int guid, long timestamp, byte tabId, GuildBankEventLogTypes eventType, long playerGuid, long itemOrMoney, short itemStackCount, byte destTabId) {
            super(guildId, guid, timestamp);
            m_eventType = eventType;
            m_bankTabId = tabId;
            m_playerGuid = playerGuid;
            m_itemOrMoney = itemOrMoney;
            m_itemStackCount = itemStackCount;
            m_destTabId = destTabId;
        }

        public static boolean isMoneyEvent(GuildBankEventLogTypes eventType) {
            return eventType == GuildBankEventLogTypes.DepositMoney || eventType == GuildBankEventLogTypes.WithdrawMoney || eventType == GuildBankEventLogTypes.RepairMoney || eventType == GuildBankEventLogTypes.CashFlowDeposit;
        }

        @Override
        public void saveToDB(SQLTransaction trans) {
            byte index = 0;

            var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GUILD_BANK_EVENTLOG);
            stmt.AddValue(index, m_guildId);
            stmt.AddValue(++index, m_guid);
            stmt.AddValue(++index, m_bankTabId);
            trans.append(stmt);

            index = 0;
            stmt = DB.characters.GetPreparedStatement(CharStatements.INS_GUILD_BANK_EVENTLOG);
            stmt.AddValue(index, m_guildId);
            stmt.AddValue(++index, m_guid);
            stmt.AddValue(++index, m_bankTabId);
            stmt.AddValue(++index, (byte) m_eventType.getValue());
            stmt.AddValue(++index, m_playerGuid);
            stmt.AddValue(++index, m_itemOrMoney);
            stmt.AddValue(++index, m_itemStackCount);
            stmt.AddValue(++index, m_destTabId);
            stmt.AddValue(++index, m_timestamp);
            trans.append(stmt);
        }

        public final void writePacket(GuildBankLogQueryResults packet) {
            var logGuid = ObjectGuid.create(HighGuid.Player, m_playerGuid);

            var hasItem = m_eventType == GuildBankEventLogTypes.DepositItem || m_eventType == GuildBankEventLogTypes.WithdrawItem || m_eventType == GuildBankEventLogTypes.MoveItem || m_eventType == GuildBankEventLogTypes.MoveItem2;

            var itemMoved = (m_eventType == GuildBankEventLogTypes.MoveItem || m_eventType == GuildBankEventLogTypes.MoveItem2);

            var hasStack = (hasItem && m_itemStackCount > 1) || itemMoved;

            GuildBankLogEntry bankLogEntry = new GuildBankLogEntry();
            bankLogEntry.playerGUID = logGuid;
            bankLogEntry.timeOffset = (int) (GameTime.getGameTime() - m_timestamp);
            bankLogEntry.entryType = (byte) m_eventType.getValue();

            if (hasStack) {
                bankLogEntry.count = m_itemStackCount;
            }

            if (isMoneyEvent()) {
                bankLogEntry.money = m_itemOrMoney;
            }

            if (hasItem) {
                bankLogEntry.itemID = (int) m_itemOrMoney;
            }

            if (itemMoved) {
                bankLogEntry.otherTab = (byte) m_destTabId;
            }

            packet.entry.add(bankLogEntry);
        }

        private boolean isMoneyEvent() {
            return isMoneyEvent(m_eventType);
        }
    }

    public static class NewsLogEntry extends LogEntry {
        private final GuildNews m_type;
        private final int m_value;
        private final ObjectGuid m_playerGuid;
        private int m_flags;

        public NewsLogEntry(long guildId, int guid, GuildNews type, ObjectGuid playerGuid, int flags, int value) {
            super(guildId, guid);
            m_type = type;
            m_playerGuid = playerGuid;
            m_flags = (int) flags;
            m_value = value;
        }

        public NewsLogEntry(long guildId, int guid, long timestamp, GuildNews type, ObjectGuid playerGuid, int flags, int value) {
            super(guildId, guid, timestamp);
            m_type = type;
            m_playerGuid = playerGuid;
            m_flags = (int) flags;
            m_value = value;
        }

        public final GuildNews getNewsType() {
            return m_type;
        }

        public final ObjectGuid getPlayerGuid() {
            return m_playerGuid;
        }

        public final int getValue() {
            return m_value;
        }

        public final int getFlags() {
            return m_flags;
        }

        public final void setSticky(boolean sticky) {
            if (sticky) {
                m_flags |= 1;
            } else {
                m_flags &= ~1;
            }
        }

        @Override
        public void saveToDB(SQLTransaction trans) {
            byte index = 0;
            var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_GUILD_NEWS);
            stmt.AddValue(index, m_guildId);
            stmt.AddValue(++index, getGUID());
            stmt.AddValue(++index, (byte) getNewsType().getValue());
            stmt.AddValue(++index, getPlayerGuid().getCounter());
            stmt.AddValue(++index, getFlags());
            stmt.AddValue(++index, getValue());
            stmt.AddValue(++index, getTimestamp());
            DB.characters.ExecuteOrAppend(trans, stmt);
        }

        public final void writePacket(GuildNewsPkt newsPacket) {
            GuildNewsEvent newsEvent = new GuildNewsEvent();
            newsEvent.id = (int) getGUID();
            newsEvent.memberGuid = getPlayerGuid();
            newsEvent.completedDate = (int) getTimestamp();
            newsEvent.flags = getFlags();
            newsEvent.type = getNewsType().getValue();

            //for (public byte i = 0; i < 2; i++)
            //    newsEvent.Data[i] =

            //newsEvent.memberList.push_back(memberGuid);

            if (getNewsType() == GuildNews.ItemLooted || getNewsType() == GuildNews.ItemCrafted || getNewsType() == GuildNews.ItemPurchased) {
                ItemInstance itemInstance = new itemInstance();
                itemInstance.itemID = getValue();
                newsEvent.item = itemInstance;
            }

            newsPacket.newsEvents.add(newsEvent);
        }
    }

    public static class LogHolder<T extends LogEntry> {
        private final ArrayList<T> m_log = new ArrayList<>();
        private final int m_maxRecords;
        private int m_nextGUID;

        public LogHolder() {
            m_maxRecords = WorldConfig.getUIntValue(T.class == BankEventLogEntry.class ? WorldCfg.GuildBankEventLogCount : WorldCfg.GuildEventLogCount);
            m_nextGUID = GuildConst.EventLogGuidUndefined;
        }

        // Checks if new log entry can be added to holder
        public final boolean canInsert() {
            return m_log.size() < m_maxRecords;
        }

        public final byte getSize() {
            return (byte) m_log.size();
        }

        public final void loadEvent(T entry) {
            if (m_nextGUID == GuildConst.EventLogGuidUndefined) {
                m_nextGUID = entry.getGUID();
            }

            m_log.add(0, entry);
        }

        public final T addEvent(SQLTransaction trans, T entry) {
            // Check max records limit
            if (!canInsert()) {
                m_log.remove(0);
            }

            // Add event to list
            m_log.add(entry);

            // Save to DB
            entry.saveToDB(trans);

            return entry;
        }

        public final int getNextGUID() {
            if (m_nextGUID == GuildConst.EventLogGuidUndefined) {
                m_nextGUID = 0;
            } else {
                m_nextGUID = (m_nextGUID + 1) % m_maxRecords;
            }

            return m_nextGUID;
        }

        public final ArrayList<T> getGuildLog() {
            return m_log;
        }
    }

    public static class RankInfo {
        private final long m_guildId;
        private final GuildBankRightsAndSlots[] m_bankTabRightsAndSlots = new GuildBankRightsAndSlots[GuildConst.MaxBankTabs];
        private GuildRankId m_rankId = GuildRankId.values()[0];
        private GuildRankOrder m_rankOrder = GuildRankOrder.values()[0];
        private String m_name;
        private GuildRankRights m_rights = GuildRankRights.values()[0];
        private int m_bankMoneyPerDay;


        public RankInfo() {
            this(0);
        }

        public RankInfo(long guildId) {
            m_guildId = guildId;
            m_rankId = GuildRankId.forValue(0xFF);
            m_rankOrder = GuildRankOrder.forValue(0);
            m_rights = GuildRankRights.NONE;
            m_bankMoneyPerDay = 0;

            for (var i = 0; i < GuildConst.MaxBankTabs; ++i) {
                m_bankTabRightsAndSlots[i] = new GuildBankRightsAndSlots();
            }
        }

        public RankInfo(long guildId, GuildRankId rankId, GuildRankOrder rankOrder, String name, GuildRankRights rights, int money) {
            m_guildId = guildId;
            m_rankId = rankId;
            m_rankOrder = rankOrder;
            m_name = name;
            m_rights = rights;
            m_bankMoneyPerDay = money;

            for (var i = 0; i < GuildConst.MaxBankTabs; ++i) {
                m_bankTabRightsAndSlots[i] = new GuildBankRightsAndSlots();
            }
        }

        public final void loadFromDB(SQLFields field) {
            m_rankId = GuildRankId.forValue(field.<Byte>Read(1));
            m_rankOrder = GuildRankOrder.forValue(field.<Byte>Read(2));
            m_name = field.<String>Read(3);
            m_rights = GuildRankRights.forValue(field.<Integer>Read(4));
            m_bankMoneyPerDay = field.<Integer>Read(5);

            if (m_rankId == GuildRankId.GuildMaster) // Prevent loss of leader rights
            {
                m_rights = GuildRankRights.forValue(m_rights.getValue() | GuildRankRights.All.getValue());
            }
        }

        public final void saveToDB(SQLTransaction trans) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_GUILD_RANK);
            stmt.AddValue(0, m_guildId);
            stmt.AddValue(1, (byte) m_rankId.getValue());
            stmt.AddValue(2, (byte) m_rankOrder.getValue());
            stmt.AddValue(3, m_name);
            stmt.AddValue(4, (int) m_rights.getValue());
            stmt.AddValue(5, m_bankMoneyPerDay);
            DB.characters.ExecuteOrAppend(trans, stmt);
        }


        public final void createMissingTabsIfNeeded(byte tabs, SQLTransaction trans) {
            createMissingTabsIfNeeded(tabs, trans, false);
        }

        public final void createMissingTabsIfNeeded(byte tabs, SQLTransaction trans, boolean logOnCreate) {
            for (byte i = 0; i < tabs; ++i) {
                var rightsAndSlots = m_bankTabRightsAndSlots[i];

                if (rightsAndSlots.getTabId() == i) {
                    continue;
                }

                rightsAndSlots.setTabId(i);

                if (m_rankId == GuildRankId.GuildMaster) {
                    rightsAndSlots.setGuildMasterValues();
                }

                if (logOnCreate) {
                    Log.outError(LogFilter.guild, String.format("Guild %1$s has broken Tab %2$s for rank %3$s. Created default tab.", m_guildId, i, m_rankId));
                }

                var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_GUILD_BANK_RIGHT);
                stmt.AddValue(0, m_guildId);
                stmt.AddValue(1, i);
                stmt.AddValue(2, (byte) m_rankId.getValue());
                stmt.AddValue(3, (byte) rightsAndSlots.getRights().getValue());
                stmt.AddValue(4, rightsAndSlots.getSlots());
                trans.append(stmt);
            }
        }

        public final void setBankTabSlotsAndRights(GuildBankRightsAndSlots rightsAndSlots, boolean saveToDB) {
            if (m_rankId == GuildRankId.GuildMaster) // Prevent loss of leader rights
            {
                rightsAndSlots.setGuildMasterValues();
            }

            m_bankTabRightsAndSlots[rightsAndSlots.getTabId()] = rightsAndSlots;

            if (saveToDB) {
                var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_GUILD_BANK_RIGHT);
                stmt.AddValue(0, m_guildId);
                stmt.AddValue(1, rightsAndSlots.getTabId());
                stmt.AddValue(2, (byte) m_rankId.getValue());
                stmt.AddValue(3, (byte) rightsAndSlots.getRights().getValue());
                stmt.AddValue(4, rightsAndSlots.getSlots());
                DB.characters.execute(stmt);
            }
        }

        public final GuildRankId getId() {
            return m_rankId;
        }

        public final GuildRankOrder getOrder() {
            return m_rankOrder;
        }

        public final void setOrder(GuildRankOrder rankOrder) {
            m_rankOrder = rankOrder;
        }

        public final String getName() {
            return m_name;
        }

        public final void setName(String name) {
            if (Objects.equals(m_name, name)) {
                return;
            }

            m_name = name;

            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GUILD_RANK_NAME);
            stmt.AddValue(0, m_name);
            stmt.AddValue(1, (byte) m_rankId.getValue());
            stmt.AddValue(2, m_guildId);
            DB.characters.execute(stmt);
        }

        public final GuildRankRights getRights() {
            return m_rights;
        }

        public final void setRights(GuildRankRights rights) {
            if (m_rankId == GuildRankId.GuildMaster) // Prevent loss of leader rights
            {
                rights = GuildRankRights.All;
            }

            if (m_rights == rights) {
                return;
            }

            m_rights = rights;

            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GUILD_RANK_RIGHTS);
            stmt.AddValue(0, (int) m_rights.getValue());
            stmt.AddValue(1, (byte) m_rankId.getValue());
            stmt.AddValue(2, m_guildId);
            DB.characters.execute(stmt);
        }

        public final int getBankMoneyPerDay() {
            return m_rankId != GuildRankId.GuildMaster ? m_bankMoneyPerDay : GuildConst.WithdrawMoneyUnlimited;
        }

        public final void setBankMoneyPerDay(int money) {
            if (m_bankMoneyPerDay == money) {
                return;
            }

            m_bankMoneyPerDay = money;

            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GUILD_RANK_BANK_MONEY);
            stmt.AddValue(0, money);
            stmt.AddValue(1, (byte) m_rankId.getValue());
            stmt.AddValue(2, m_guildId);
            DB.characters.execute(stmt);
        }

        public final GuildBankRights getBankTabRights(byte tabId) {
            return tabId < GuildConst.MaxBankTabs ? m_bankTabRightsAndSlots[tabId].getRights() : 0;
        }

        public final int getBankTabSlotsPerDay(byte tabId) {
            return tabId < GuildConst.MaxBankTabs ? m_bankTabRightsAndSlots[tabId].getSlots() : 0;
        }
    }

    public static class BankTab {
        private final long m_guildId;
        private final byte m_tabId;
        private final Item[] m_items = new Item[GuildConst.MaxBankSlots];
        private String m_name;
        private String m_icon;
        private String m_text;

        public bankTab(long guildId, byte tabId) {
            m_guildId = guildId;
            m_tabId = tabId;
        }

        public final void loadFromDB(SQLFields field) {
            m_name = field.<String>Read(2);
            m_icon = field.<String>Read(3);
            m_text = field.<String>Read(4);
        }

        public final boolean loadItemFromDB(SQLFields field) {
            var slotId = field.<Byte>Read(53);
            var itemGuid = field.<Integer>Read(0);
            var itemEntry = field.<Integer>Read(1);

            if (slotId >= GuildConst.MaxBankSlots) {
                Log.outError(LogFilter.guild, "Invalid slot for item (GUID: {0}, id: {1}) in guild bank, skipped.", itemGuid, itemEntry);

                return false;
            }

            var proto = global.getObjectMgr().getItemTemplate(itemEntry);

            if (proto == null) {
                Log.outError(LogFilter.guild, "Unknown item (GUID: {0}, id: {1}) in guild bank, skipped.", itemGuid, itemEntry);

                return false;
            }

            var pItem = item.newItemOrBag(proto);

            if (!pItem.loadFromDB(itemGuid, ObjectGuid.Empty, field, itemEntry)) {
                Log.outError(LogFilter.guild, "Item (GUID {0}, id: {1}) not found in item_instance, deleting from guild bank!", itemGuid, itemEntry);

                var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_NONEXISTENT_GUILD_BANK_ITEM);
                stmt.AddValue(0, m_guildId);
                stmt.AddValue(1, m_tabId);
                stmt.AddValue(2, slotId);
                DB.characters.execute(stmt);

                return false;
            }

            pItem.addToWorld();
            m_items[slotId] = pItem;

            return true;
        }


        public final void delete(SQLTransaction trans) {
            delete(trans, false);
        }

        public final void delete(SQLTransaction trans, boolean removeItemsFromDB) {
            for (byte slotId = 0; slotId < GuildConst.MaxBankSlots; ++slotId) {
                var pItem = m_items[slotId];

                if (pItem != null) {
                    pItem.removeFromWorld();

                    if (removeItemsFromDB) {
                        pItem.deleteFromDB(trans);
                    }
                }
            }
        }

        public final void setInfo(String name, String icon) {
            if (Objects.equals(m_name, name) && Objects.equals(m_icon, icon)) {
                return;
            }

            m_name = name;
            m_icon = icon;

            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GUILD_BANK_TAB_INFO);
            stmt.AddValue(0, m_name);
            stmt.AddValue(1, m_icon);
            stmt.AddValue(2, m_guildId);
            stmt.AddValue(3, m_tabId);
            DB.characters.execute(stmt);
        }

        public final void sendText(Guild guild) {
            sendText(guild, null);
        }

        public final void sendText(Guild guild, WorldSession session) {
            GuildBankTextQueryResult textQuery = new GuildBankTextQueryResult();
            textQuery.tab = m_tabId;
            textQuery.text = m_text;

            if (session != null) {
                Log.outDebug(LogFilter.guild, "SMSG_GUILD_BANK_QUERY_TEXT_RESULT [{0}]: Tabid: {1}, Text: {2}", session.getPlayerInfo(), m_tabId, m_text);
                session.sendPacket(textQuery);
            } else {
                Log.outDebug(LogFilter.guild, "SMSG_GUILD_BANK_QUERY_TEXT_RESULT [Broadcast]: Tabid: {0}, Text: {1}", m_tabId, m_text);
                guild.broadcastPacket(textQuery);
            }
        }

        public final String getName() {
            return m_name;
        }

        public final String getIcon() {
            return m_icon;
        }

        public final String getText() {
            return m_text;
        }

        public final void setText(String text) {
            if (Objects.equals(m_text, text)) {
                return;
            }

            m_text = text;

            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GUILD_BANK_TAB_TEXT);
            stmt.AddValue(0, m_text);
            stmt.AddValue(1, m_guildId);
            stmt.AddValue(2, m_tabId);
            DB.characters.execute(stmt);
        }

        public final Item getItem(byte slotId) {
            return slotId < GuildConst.MaxBankSlots ? m_items[slotId] : null;
        }

        public final boolean setItem(SQLTransaction trans, byte slotId, Item item) {
            if (slotId >= GuildConst.MaxBankSlots) {
                return false;
            }

            m_items[slotId] = item;

            var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GUILD_BANK_ITEM);
            stmt.AddValue(0, m_guildId);
            stmt.AddValue(1, m_tabId);
            stmt.AddValue(2, slotId);
            trans.append(stmt);

            if (item != null) {
                stmt = DB.characters.GetPreparedStatement(CharStatements.INS_GUILD_BANK_ITEM);
                stmt.AddValue(0, m_guildId);
                stmt.AddValue(1, m_tabId);
                stmt.AddValue(2, slotId);
                stmt.AddValue(3, item.getGUID().getCounter());
                trans.append(stmt);

                item.setContainedIn(ObjectGuid.Empty);
                item.setOwnerGUID(ObjectGuid.Empty);
                item.FSetState(ItemUpdateState.New);
                item.saveToDB(trans); // Not in inventory and can be saved standalone
            }

            return true;
        }
    }

    public static class GuildBankRightsAndSlots {
        private byte tabId;
        private GuildBankRights rights = GuildBankRights.values()[0];
        private int slots;


        public GuildBankRightsAndSlots(byte tabId, byte rights) {
            this(tabId, rights, 0);
        }

        public GuildBankRightsAndSlots(byte tabId) {
            this(tabId, 0, 0);
        }

        public GuildBankRightsAndSlots() {
            this((byte) 0xFF, 0, 0);
        }

        public GuildBankRightsAndSlots(byte tabId, byte rights, int slots) {
            tabId = tabId;
            rights = GuildBankRights.forValue(rights);
            slots = slots;
        }

        public final void setGuildMasterValues() {
            rights = GuildBankRights.Full;
            slots = (int) GuildConst.WithdrawSlotUnlimited;
        }

        public final byte getTabId() {
            return tabId;
        }

        public final void setTabId(byte tabId) {
            tabId = tabId;
        }

        public final int getSlots() {
            return slots;
        }

        public final void setSlots(int slots) {
            slots = slots;
        }

        public final GuildBankRights getRights() {
            return rights;
        }

        public final void setRights(GuildBankRights rights) {
            rights = rights;
        }
    }

    public static class EmblemInfo {
        private int m_style;
        private int m_color;
        private int m_borderStyle;
        private int m_borderColor;
        private int m_backgroundColor;

        public emblemInfo() {
            m_style = 0;
            m_color = 0;
            m_borderStyle = 0;
            m_borderColor = 0;
            m_backgroundColor = 0;
        }

        public final void readPacket(SaveGuildEmblem packet) {
            m_style = packet.EStyle;
            m_color = packet.EColor;
            m_borderStyle = packet.BStyle;
            m_borderColor = packet.BColor;
            m_backgroundColor = packet.bg;
        }

        public final boolean validateEmblemColors() {
            return CliDB.GuildColorBackgroundStorage.containsKey(m_backgroundColor) && CliDB.GuildColorBorderStorage.containsKey(m_borderColor) && CliDB.GuildColorEmblemStorage.containsKey(m_color);
        }

        public final boolean loadFromDB(SQLFields field) {
            m_style = field.<Byte>Read(3);
            m_color = field.<Byte>Read(4);
            m_borderStyle = field.<Byte>Read(5);
            m_borderColor = field.<Byte>Read(6);
            m_backgroundColor = field.<Byte>Read(7);

            return validateEmblemColors();
        }

        public final void saveToDB(long guildId) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GUILD_EMBLEM_INFO);
            stmt.AddValue(0, m_style);
            stmt.AddValue(1, m_color);
            stmt.AddValue(2, m_borderStyle);
            stmt.AddValue(3, m_borderColor);
            stmt.AddValue(4, m_backgroundColor);
            stmt.AddValue(5, guildId);
            DB.characters.execute(stmt);
        }

        public final int getStyle() {
            return m_style;
        }

        public final int getColor() {
            return m_color;
        }

        public final int getBorderStyle() {
            return m_borderStyle;
        }

        public final int getBorderColor() {
            return m_borderColor;
        }

        public final int getBackgroundColor() {
            return m_backgroundColor;
        }
    }

    public abstract static class MoveItemData {
        public Guild m_pGuild;
        public Player m_pPlayer;
        public byte m_container;
        public byte m_slotId;
        public Item m_pItem;
        public Item m_pClonedItem;
        public ArrayList<ItemPosCount> m_vec = new ArrayList<>();

        protected MoveItemData(Guild guild, Player player, byte container, byte slotId) {
            m_pGuild = guild;
            m_pPlayer = player;
            m_container = container;
            m_slotId = slotId;
            m_pItem = null;
            m_pClonedItem = null;
        }

        public boolean checkItem(tangible.RefObject<Integer> splitedAmount) {
            if (splitedAmount.refArgValue > m_pItem.getCount()) {
                return false;
            }

            if (splitedAmount.refArgValue == m_pItem.getCount()) {
                splitedAmount.refArgValue = 0;
            }

            return true;
        }

        public final InventoryResult canStore(Item pItem, boolean swap, boolean sendError) {
            m_vec.clear();
            var msg = canStore(pItem, swap);

            if (sendError && msg != InventoryResult.Ok) {
                sendEquipError(msg, pItem);
            }

            return msg;
        }

        public final boolean cloneItem(int count) {
            m_pClonedItem = m_pItem.cloneItem(count);

            if (m_pClonedItem == null) {
                sendEquipError(InventoryResult.ItemNotFound, m_pItem);

                return false;
            }

            return true;
        }

        public void logAction(MoveItemData pFrom) {
            global.getScriptMgr().<IGuildOnItemMove>ForEach(p -> p.OnItemMove(m_pGuild, m_pPlayer, pFrom.getItem(), pFrom.isBank(), pFrom.getContainer(), pFrom.getSlotId(), isBank(), getContainer(), getSlotId()));
        }

        public final void copySlots(ArrayList<Byte> ids) {
            for (var item : m_vec) {
                ids.add((byte) item.pos);
            }
        }

        public final void sendEquipError(InventoryResult result, Item item) {
            m_pPlayer.sendEquipError(result, item);
        }

        public abstract boolean isBank();

        // Initializes item. Returns true, if item exists, false otherwise.
        public abstract boolean initItem();

        // Checks splited amount against item. Splited amount cannot be more that number of items in stack.
        // Defines if player has rights to save item in container
        public boolean hasStoreRights(MoveItemData pOther) {
            return true;
        }

        // Defines if player has rights to withdraw item from container
        public boolean hasWithdrawRights(MoveItemData pOther) {
            return true;
        }

        // Remove item from container (if splited update items fields)

        public final abstract void removeItem(SQLTransaction trans, MoveItemData pOther);

        public abstract void removeItem(SQLTransaction trans, MoveItemData pOther, int splitedAmount);

        // Saves item to container
        public abstract Item storeItem(SQLTransaction trans, Item pItem);

        // Log bank event
        public abstract void logBankEvent(SQLTransaction trans, MoveItemData pFrom, int count);

        public abstract InventoryResult canStore(Item pItem, boolean swap);


        public final Item getItem() {
            return getItem(false);
        }

        public final Item getItem(boolean isCloned) {
            return isCloned ? m_pClonedItem : m_pItem;
        }

        public final byte getContainer() {
            return m_container;
        }

        public final byte getSlotId() {
            return m_slotId;
        }
    }

    public static class PlayerMoveItemData extends MoveItemData {
        public PlayerMoveItemData(Guild guild, Player player, byte container, byte slotId) {
            super(guild, player, container, slotId);
        }

        @Override
        public boolean isBank() {
            return false;
        }

        @Override
        public boolean initItem() {
            m_pItem = m_pPlayer.getItemByPos(m_container, m_slotId);

            if (m_pItem != null) {
                // Anti-WPE protection. Do not move non-empty bags to bank.
                if (m_pItem.isNotEmptyBag()) {
                    sendEquipError(InventoryResult.DestroyNonemptyBag, m_pItem);
                    m_pItem = null;
                }
                // Bound items cannot be put into bank.
                else if (!m_pItem.canBeTraded()) {
                    sendEquipError(InventoryResult.CantSwap, m_pItem);
                    m_pItem = null;
                }
            }

            return (m_pItem != null);
        }


        @Override
        public void removeItem(SQLTransaction trans, MoveItemData pOther) {
            removeItem(trans, pOther, 0);
        }

        @Override
        public void removeItem(SQLTransaction trans, MoveItemData pOther, int splitedAmount) {
            if (splitedAmount != 0) {
                m_pItem.setCount(m_pItem.getCount() - splitedAmount);
                m_pItem.setState(ItemUpdateState.changed, m_pPlayer);
                m_pPlayer.saveInventoryAndGoldToDB(trans);
            } else {
                m_pPlayer.moveItemFromInventory(m_container, m_slotId, true);
                m_pItem.deleteFromInventoryDB(trans);
                m_pItem = null;
            }
        }

        @Override
        public Item storeItem(SQLTransaction trans, Item pItem) {
            m_pPlayer.moveItemToInventory(m_vec, pItem, true);
            m_pPlayer.saveInventoryAndGoldToDB(trans);

            return pItem;
        }

        @Override
        public void logBankEvent(SQLTransaction trans, MoveItemData pFrom, int count) {
            // Bank . Char
            m_pGuild._LogBankEvent(trans, GuildBankEventLogTypes.WithdrawItem, pFrom.getContainer(), m_pPlayer.getGUID().getCounter(), pFrom.getItem().getEntry(), (short) count);
        }

        @Override
        public InventoryResult canStore(Item pItem, boolean swap) {
            return m_pPlayer.canStoreItem(m_container, m_slotId, m_vec, pItem, swap);
        }
    }

    public static class BankMoveItemData extends MoveItemData {
        public BankMoveItemData(Guild guild, Player player, byte container, byte slotId) {
            super(guild, player, container, slotId);
        }

        @Override
        public boolean isBank() {
            return true;
        }

        @Override
        public boolean initItem() {
            m_pItem = m_pGuild._GetItem(m_container, m_slotId);

            return (m_pItem != null);
        }

        @Override
        public boolean hasStoreRights(MoveItemData pOther) {
            // Do not check rights if item is being swapped within the same bank tab
            if (pOther.isBank() && pOther.getContainer() == m_container) {
                return true;
            }

            return m_pGuild._MemberHasTabRights(m_pPlayer.getGUID(), m_container, GuildBankRights.DepositItem);
        }

        @Override
        public boolean hasWithdrawRights(MoveItemData pOther) {
            // Do not check rights if item is being swapped within the same bank tab
            if (pOther.isBank() && pOther.getContainer() == m_container) {
                return true;
            }

            var slots = 0;
            var member = m_pGuild.getMember(m_pPlayer.getGUID());

            if (member != null) {
                slots = m_pGuild._GetMemberRemainingSlots(member, m_container);
            }

            return slots != 0;
        }


        @Override
        public void removeItem(SQLTransaction trans, MoveItemData pOther) {
            removeItem(trans, pOther, 0);
        }

        @Override
        public void removeItem(SQLTransaction trans, MoveItemData pOther, int splitedAmount) {
            if (splitedAmount != 0) {
                m_pItem.setCount(m_pItem.getCount() - splitedAmount);
                m_pItem.FSetState(ItemUpdateState.changed);
                m_pItem.saveToDB(trans);
            } else {
                m_pGuild._RemoveItem(trans, m_container, m_slotId);
                m_pItem = null;
            }

            // Decrease amount of player's remaining items (if item is moved to different tab or to player)
            if (!pOther.isBank() || pOther.getContainer() != m_container) {
                m_pGuild._UpdateMemberWithdrawSlots(trans, m_pPlayer.getGUID(), m_container);
            }
        }

        @Override
        public Item storeItem(SQLTransaction trans, Item pItem) {
            if (pItem == null) {
                return null;
            }

            var pTab = m_pGuild.getBankTab(m_container);

            if (pTab == null) {
                return null;
            }

            var pLastItem = pItem;

            for (var pos : m_vec) {
                Log.outDebug(LogFilter.guild, "GUILD STORAGE: StoreItem tab = {0}, slot = {1}, item = {2}, count = {3}", m_container, m_slotId, pItem.getEntry(), pItem.getCount());

                pLastItem = _StoreItem(trans, pTab, pItem, pos, pos.equals(m_vec.get(m_vec.size() - 1)));
            }

            return pLastItem;
        }

        @Override
        public void logBankEvent(SQLTransaction trans, MoveItemData pFrom, int count) {
            if (pFrom.isBank()) {
                // Bank . Bank
                m_pGuild._LogBankEvent(trans, GuildBankEventLogTypes.MoveItem, pFrom.getContainer(), m_pPlayer.getGUID().getCounter(), pFrom.getItem().getEntry(), (short) count, m_container);
            } else {
                // Char . Bank
                m_pGuild._LogBankEvent(trans, GuildBankEventLogTypes.DepositItem, m_container, m_pPlayer.getGUID().getCounter(), pFrom.getItem().getEntry(), (short) count);
            }
        }

        @Override
        public void logAction(MoveItemData pFrom) {
            super.logAction(pFrom);

            if (!pFrom.isBank() && m_pPlayer.getSession().hasPermission(RBACPermissions.LogGmTrade)) // @todo Move this to scripts
            {
                Log.outCommand(m_pPlayer.getSession().getAccountId(), "GM {0} ({1}) (Account: {2}) deposit item: {3} (Entry: {4} Count: {5}) to guild bank named: {6} (Guild ID: {7})", m_pPlayer.getName(), m_pPlayer.getGUID().toString(), m_pPlayer.getSession().getAccountId(), pFrom.getItem().getTemplate().getName(), pFrom.getItem().getEntry(), pFrom.getItem().getCount(), m_pGuild.getName(), m_pGuild.getId());
            }
        }

        @Override
        public InventoryResult canStore(Item pItem, boolean swap) {
            Log.outDebug(LogFilter.guild, "GUILD STORAGE: canStore() tab = {0}, slot = {1}, item = {2}, count = {3}", m_container, m_slotId, pItem.getEntry(), pItem.getCount());

            var count = pItem.getCount();

            // Soulbound items cannot be moved
            if (pItem.isSoulBound()) {
                return InventoryResult.DropBoundItem;
            }

            // Make sure destination bank tab exists
            if (m_container >= m_pGuild._GetPurchasedTabsSize()) {
                return InventoryResult.WrongBagType;
            }

            // Slot explicitely specified. Check it.
            if (m_slotId != ItemConst.NullSlot) {
                var pItemDest = m_pGuild._GetItem(m_container, m_slotId);

                // Ignore swapped item (this slot will be empty after move)
                if ((pItemDest == pItem) || swap) {
                    pItemDest = null;
                }

                tangible.RefObject<Integer> tempRef_count = new tangible.RefObject<Integer>(count);
                if (!_ReserveSpace(m_slotId, pItem, pItemDest, tempRef_count)) {
                    count = tempRef_count.refArgValue;
                    return InventoryResult.CantStack;
                } else {
                    count = tempRef_count.refArgValue;
                }

                if (count == 0) {
                    return InventoryResult.Ok;
                }
            }

            // Slot was not specified or it has not enough space for all the items in stack
            // Search for stacks to merge with
            if (pItem.getMaxStackCount() > 1) {
                tangible.RefObject<Integer> tempRef_count2 = new tangible.RefObject<Integer>(count);
                canStoreItemInTab(pItem, m_slotId, true, tempRef_count2);
                count = tempRef_count2.refArgValue;

                if (count == 0) {
                    return InventoryResult.Ok;
                }
            }

            // Search free slot for item
            tangible.RefObject<Integer> tempRef_count3 = new tangible.RefObject<Integer>(count);
            canStoreItemInTab(pItem, m_slotId, false, tempRef_count3);
            count = tempRef_count3.refArgValue;

            if (count == 0) {
                return InventoryResult.Ok;
            }

            return InventoryResult.BankFull;
        }

        private Item _StoreItem(SQLTransaction trans, BankTab pTab, Item pItem, ItemPosCount pos, boolean clone) {
            var slotId = (byte) pos.pos;
            var count = pos.count;
            var pItemDest = pTab.getItem(slotId);

            if (pItemDest != null) {
                pItemDest.setCount(pItemDest.getCount() + count);
                pItemDest.FSetState(ItemUpdateState.changed);
                pItemDest.saveToDB(trans);

                if (!clone) {
                    pItem.removeFromWorld();
                    pItem.deleteFromDB(trans);
                }

                return pItemDest;
            }

            if (clone) {
                pItem = pItem.cloneItem(count);
            } else {
                pItem.setCount(count);
            }

            if (pItem != null && pTab.setItem(trans, slotId, pItem)) {
                return pItem;
            }

            return null;
        }

        private boolean _ReserveSpace(byte slotId, Item pItem, Item pItemDest, tangible.RefObject<Integer> count) {
            var requiredSpace = pItem.getMaxStackCount();

            if (pItemDest != null) {
                // Make sure source and destination items match and destination item has space for more stacks.
                if (pItemDest.getEntry() != pItem.getEntry() || pItemDest.getCount() >= pItem.getMaxStackCount()) {
                    return false;
                }

                requiredSpace -= pItemDest.getCount();
            }

            // Let's not be greedy, reserve only required space
            requiredSpace = Math.min(requiredSpace, count.refArgValue);

            // Reserve space
            ItemPosCount pos = new ItemPosCount(slotId, requiredSpace);

            if (!pos.isContainedIn(m_vec)) {
                m_vec.add(pos);
                count.refArgValue -= requiredSpace;
            }

            return true;
        }

        private void canStoreItemInTab(Item pItem, byte skipSlotId, boolean merge, tangible.RefObject<Integer> count) {
            for (byte slotId = 0; (slotId < GuildConst.MaxBankSlots) && (count.refArgValue > 0); ++slotId) {
                // Skip slot already processed in CanStore (when destination slot was specified)
                if (slotId == skipSlotId) {
                    continue;
                }

                var pItemDest = m_pGuild._GetItem(m_container, slotId);

                if (pItemDest == pItem) {
                    pItemDest = null;
                }

                // If merge skip empty, if not merge skip non-empty
                if ((pItemDest != null) != merge) {
                    continue;
                }

                _ReserveSpace(slotId, pItem, pItemDest, count);
            }
        }
    }


    ///#endregion
}
