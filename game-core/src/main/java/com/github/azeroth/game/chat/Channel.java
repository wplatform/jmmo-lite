package com.github.azeroth.game.chat;


import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.map.LocalizedDo;
import com.github.azeroth.game.networking.packet.ChannelListResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.locale;


public class Channel {
    private final ChannelFlags channelFlags;
    private final int channelId;
    private final Team channelTeam;
    private final String channelName;
    private final HashMap<ObjectGuid, PlayerInfo> playersStore = new HashMap<ObjectGuid, PlayerInfo>();
    private final ArrayList<ObjectGuid> bannedStore = new ArrayList<>();
    private final AreaTableRecord zoneEntry;
    private final ObjectGuid channelGuid;

    private boolean isDirty; // whether the channel needs to be saved to DB
    private long nextActivityUpdateTime;

    private boolean announceEnabled;
    private boolean ownershipEnabled;
    private boolean isOwnerInvisible;
    private ObjectGuid ownerGuid = ObjectGuid.EMPTY;
    private String channelPassword;


    public channel(ObjectGuid guid, int channelId, Team team) {
        this(guid, channelId, team, null);
    }

    public channel(ObjectGuid guid, int channelId) {
        this(guid, channelId, 0, null);
    }

    public channel(ObjectGuid guid, int channelId, Team team, AreaTableRecord zoneEntry) {
        channelFlags = channelFlags.General;
        channelId = channelId;
        channelTeam = team;
        channelGuid = guid;
        zoneEntry = zoneEntry;

        var channelEntry = CliDB.ChatChannelsStorage.get(channelId);

        if (channelEntry.flags.hasFlag(ChannelDBCFlags.Trade)) // for trade channel
        {
            channelFlags = channelFlags.forValue(channelFlags.getValue() | channelFlags.Trade.getValue());
        }

        if (channelEntry.flags.hasFlag(ChannelDBCFlags.CityOnly2)) // for city only channels
        {
            channelFlags = channelFlags.forValue(channelFlags.getValue() | channelFlags.City.getValue());
        }

        if (channelEntry.flags.hasFlag(ChannelDBCFlags.Lfg)) // for LFG channel
        {
            channelFlags = channelFlags.forValue(channelFlags.getValue() | channelFlags.Lfg.getValue());
        } else // for all other channels
        {
            channelFlags = channelFlags.forValue(channelFlags.getValue() | channelFlags.NotLfg.getValue());
        }
    }


    public channel(ObjectGuid guid, String name, Team team) {
        this(guid, name, team, "");
    }

    public channel(ObjectGuid guid, String name) {
        this(guid, name, 0, "");
    }

    public channel(ObjectGuid guid, String name, Team team, String banList) {
        announceEnabled = true;
        ownershipEnabled = true;
        channelFlags = channelFlags.Custom;
        channelTeam = team;
        channelGuid = guid;
        channelName = name;

        LocalizedString tokens = new LocalizedString();

        for (String token : tokens) {
            // legacy db content might not have 0x prefix, account for that
            var bannedGuidStr = token.contains("0x") ? token.substring(2) : token;
            ObjectGuid banned = ObjectGuid.EMPTY;
            banned.SetRawValue(Long.parseLong(bannedGuidStr.substring(0, 16)), Long.parseLong(bannedGuidStr.substring(16)));

            if (banned.isEmpty()) {
                continue;
            }

            Log.outDebug(LogFilter.ChatSystem, String.format("Channel(%1$s) loaded player %2$s into bannedStore", name, banned));
            bannedStore.add(banned);
        }
    }

    public static void getChannelName(tangible.RefObject<String> channelName, int channelId, Locale locale, AreaTableRecord zoneEntry) {
        if (channelId != 0) {
            var channelEntry = CliDB.ChatChannelsStorage.get(channelId);

            if (!channelEntry.flags.hasFlag(ChannelDBCFlags.global)) {
                if (channelEntry.flags.hasFlag(ChannelDBCFlags.CityOnly)) {
                    channelName.refArgValue = String.format(channelEntry.name.charAt(locale).ConvertFormatSyntax(), global.getObjectMgr().getSysMessage(SysMessage.ChannelCity, locale));
                } else {
                    channelName.refArgValue = String.format(channelEntry.name.charAt(locale).ConvertFormatSyntax(), zoneEntry.AreaName.get(locale));
                }
            } else {
                channelName.refArgValue = channelEntry.name.charAt(locale);
            }
        }
    }


    public final String getName() {
        return getName(locale.enUS);
    }

    public final String getName(Locale locale) {
        var result = channelName;
        tangible.RefObject<String> tempRef_result = new tangible.RefObject<String>(result);
        getChannelName(tempRef_result, channelId, locale, zoneEntry);
        result = tempRef_result.refArgValue;

        return result;
    }

    public final void updateChannelInDB() {
        var now = gameTime.GetGameTime();

        if (isDirty) {
            var banlist = "";

            for (var iter : bannedStore) {
                banlist += iter.GetRawValue().ToHexString() + ' ';
            }

            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHANNEL);
            stmt.AddValue(0, channelName);
            stmt.AddValue(1, (int) channelTeam.getValue());
            stmt.AddValue(2, announceEnabled);
            stmt.AddValue(3, ownershipEnabled);
            stmt.AddValue(4, channelPassword);
            stmt.AddValue(5, banlist);
            DB.characters.execute(stmt);
        } else if (nextActivityUpdateTime <= now) {
            if (!playersStore.isEmpty()) {
                var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHANNEL_USAGE);
                stmt.AddValue(0, channelName);
                stmt.AddValue(1, (int) channelTeam.getValue());
                DB.characters.execute(stmt);
            }
        } else {
            return;
        }

        isDirty = false;
        nextActivityUpdateTime = now + RandomUtil.URand(1 * time.Minute, 6 * time.Minute) * Math.max(1, WorldConfig.getUIntValue(WorldCfg.PreserveCustomChannelInterval));
    }


    public final void joinChannel(Player player) {
        joinChannel(player, "");
    }

    public final void joinChannel(Player player, String pass) {
        var guid = player.getGUID();

        if (isOn(guid)) {
            // Do not send error message for built-in channels
            if (!isConstant()) {
                var builder = new ChannelNameBuilder(this, new PlayerAlreadyMemberAppend(guid));
                sendToOne(builder, guid);
            }

            return;
        }

        if (isBanned(guid)) {
            var builder = new ChannelNameBuilder(this, new BannedAppend());
            sendToOne(builder, guid);

            return;
        }

        if (!checkPassword(pass)) {
            var builder = new ChannelNameBuilder(this, new WrongPasswordAppend());
            sendToOne(builder, guid);

            return;
        }

        if (hasFlag(channelFlags.Lfg) && WorldConfig.getBoolValue(WorldCfg.RestrictedLfgChannel) && global.getAccountMgr().isPlayerAccount(player.getSession().getSecurity()) && player.getGroup()) {
            var builder = new ChannelNameBuilder(this, new NotInLFGAppend());
            sendToOne(builder, guid);

            return;
        }

        player.joinedChannel(this);

        if (announceEnabled && !player.getSession().hasPermission(RBACPermissions.SilentlyJoinChannel)) {
            var builder = new ChannelNameBuilder(this, new JoinedAppend(guid));
            sendToAll(builder);
        }

        var newChannel = playersStore.isEmpty();

        if (newChannel) {
            nextActivityUpdateTime = 0; // force activity update on next channel tick
        }

        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.setInvisible(!player.isGMVisible());
        playersStore.put(guid, playerInfo);

		/*
		ChannelNameBuilder<YouJoinedAppend> builder = new ChannelNameBuilder(this, new YouJoinedAppend());
		sendToOne(builder, guid);
		*/

        sendToOne(new ChannelNotifyJoinedBuilder(this), guid);

        joinNotify(player);

        // Custom channel handling
        if (!isConstant()) {
            // If the channel has no owner yet and ownership is allowed, set the new owner.
            // or if the owner was a GM with .gm visible off
            // don't do this if the new player is, too, an invis GM, unless the channel was empty
            if (ownershipEnabled && (newChannel || !playerInfo.isInvisible()) && (ownerGuid.isEmpty() || isOwnerInvisible)) {
                isOwnerInvisible = playerInfo.isInvisible();

                setOwner(guid, !newChannel && !isOwnerInvisible);
                playersStore.get(guid).setModerator(true);
            }
        }
    }


    public final void leaveChannel(Player player, boolean send) {
        leaveChannel(player, send, false);
    }

    public final void leaveChannel(Player player) {
        leaveChannel(player, true, false);
    }

    public final void leaveChannel(Player player, boolean send, boolean suspend) {
        var guid = player.getGUID();

        if (!isOn(guid)) {
            if (send) {
                var builder = new ChannelNameBuilder(this, new NotMemberAppend());
                sendToOne(builder, guid);
            }

            return;
        }

        player.leftChannel(this);

        if (send) {
			/*
			ChannelNameBuilder<YouLeftAppend> builder = new ChannelNameBuilder(this, new YouLeftAppend());
			sendToOne(builder, guid);
			*/
            sendToOne(new ChannelNotifyLeftBuilder(this, suspend), guid);
        }

        var info = playersStore.get(guid);
        var changeowner = info.isOwner();
        playersStore.remove(guid);

        if (announceEnabled && !player.getSession().hasPermission(RBACPermissions.SilentlyJoinChannel)) {
            var builder = new ChannelNameBuilder(this, new LeftAppend(guid));
            sendToAll(builder);
        }

        leaveNotify(player);

        if (!isConstant()) {
            // If the channel owner left and there are still playersStore inside, pick a new owner
            // do not pick invisible gm owner unless there are only invisible gms in that channel (rare)
            if (changeowner && ownershipEnabled && !playersStore.isEmpty()) {
                var newowner = ObjectGuid.Empty;

                for (var key : playersStore.keySet()) {
                    if (!playersStore.get(key).isInvisible()) {
                        newowner = key;

                        break;
                    }
                }

                if (newowner.isEmpty()) {
                    newowner = playersStore.firstEntry().key;
                }

                playersStore.get(newowner).setModerator(true);

                setOwner(newowner);

                // if the new owner is invisible gm, set flag to automatically choose a new owner
                if (playersStore.get(newowner).isInvisible()) {
                    isOwnerInvisible = true;
                }
            }
        }
    }

    public final void unBan(Player player, String badname) {
        var good = player.getGUID();

        if (!isOn(good)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new NotMemberAppend());
            sendToOne(builder, good);

            return;
        }

        var info = playersStore.get(good);

        if (!info.isModerator() && !player.getSession().hasPermission(RBACPermissions.ChangeChannelNotModerator)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new NotModeratorAppend());
            sendToOne(builder, good);

            return;
        }

        var bad = global.getObjAccessor().FindPlayerByName(badname);
        var victim = bad ? bad.getGUID() : ObjectGuid.Empty;

        if (victim.isEmpty() || !isBanned(victim)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new PlayerNotFoundAppend(badname));
            sendToOne(builder, good);

            return;
        }

        bannedStore.remove(victim);

        ChannelNameBuilder builder1 = new ChannelNameBuilder(this, new PlayerUnbannedAppend(good, victim));
        sendToAll(builder1);

        isDirty = true;
    }

    public final void password(Player player, String pass) {
        var guid = player.getGUID();

        if (!isOn(guid)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new NotMemberAppend());
            sendToOne(builder, guid);

            return;
        }

        var info = playersStore.get(guid);

        if (!info.isModerator() && !player.getSession().hasPermission(RBACPermissions.ChangeChannelNotModerator)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new NotModeratorAppend());
            sendToOne(builder, guid);

            return;
        }

        channelPassword = pass;

        ChannelNameBuilder builder1 = new ChannelNameBuilder(this, new PasswordChangedAppend(guid));
        sendToAll(builder1);

        isDirty = true;
    }

    public final void setInvisible(Player player, boolean on) {
        var playerInfo = playersStore.get(player.getGUID());

        if (playerInfo == null) {
            return;
        }

        playerInfo.setInvisible(on);

        // we happen to be owner too, update flag
        if (Objects.equals(ownerGuid, player.getGUID())) {
            isOwnerInvisible = on;
        }
    }

    public final void setOwner(Player player, String newname) {
        var guid = player.getGUID();

        if (!isOn(guid)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new NotMemberAppend());
            sendToOne(builder, guid);

            return;
        }

        if (!player.getSession().hasPermission(RBACPermissions.ChangeChannelNotModerator) && ObjectGuid.opNotEquals(guid, ownerGuid)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new NotOwnerAppend());
            sendToOne(builder, guid);

            return;
        }

        var newp = global.getObjAccessor().FindPlayerByName(newname);
        var victim = newp ? newp.getGUID() : ObjectGuid.Empty;

        if (newp == null || victim.isEmpty() || !isOn(victim) || (player.getTeam() != newp.getTeam() && (!player.getSession().hasPermission(RBACPermissions.TwoSideInteractionChannel) || !newp.getSession().hasPermission(RBACPermissions.TwoSideInteractionChannel)))) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new PlayerNotFoundAppend(newname));
            sendToOne(builder, guid);

            return;
        }

        playersStore.get(victim).setModerator(true);
        setOwner(victim);
    }

    public final void sendWhoOwner(Player player) {
        var guid = player.getGUID();

        if (isOn(guid)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new ChannelOwnerAppend(this, ownerGuid));
            sendToOne(builder, guid);
        } else {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new NotMemberAppend());
            sendToOne(builder, guid);
        }
    }

    public final void list(Player player) {
        var guid = player.getGUID();

        if (!isOn(guid)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new NotMemberAppend());
            sendToOne(builder, guid);

            return;
        }

        var channelName = getName(player.getSession().getSessionDbcLocale());
        Log.outDebug(LogFilter.ChatSystem, "SMSG_CHANNEL_LIST {0} Channel: {1}", player.getSession().getPlayerInfo(), channelName);

        ChannelListResponse list = new ChannelListResponse();
        list.display = true; // always true?
        list.channel = channelName;
        list.channelFlags = getFlags();

        var gmLevelInWhoList = WorldConfig.getUIntValue(WorldCfg.GmLevelInWhoList);

        for (var pair : playersStore.entrySet()) {
            var member = global.getObjAccessor().findConnectedPlayer(pair.getKey());

            // PLAYER can't see MODERATOR, GAME MASTER, ADMINISTRATOR character
            // MODERATOR, GAME MASTER, ADMINISTRATOR can see all
            if (member && (player.getSession().hasPermission(RBACPermissions.WhoSeeAllSecLevels) || member.getSession().getSecurity().getValue() <= AccountTypes.forValue(gmLevelInWhoList)) && member.isVisibleGloballyFor(player)) {
                list.members.add(new ChannelListResponse.ChannelPlayer(pair.getKey(), global.getWorldMgr().getVirtualRealmAddress(), pair.getValue().getFlags()));
            }
        }

        player.sendPacket(list);
    }

    public final void announce(Player player) {
        var guid = player.getGUID();

        if (!isOn(guid)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new NotMemberAppend());
            sendToOne(builder, guid);

            return;
        }

        var playerInfo = playersStore.get(guid);

        if (!playerInfo.isModerator() && !player.getSession().hasPermission(RBACPermissions.ChangeChannelNotModerator)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new NotModeratorAppend());
            sendToOne(builder, guid);

            return;
        }

        announceEnabled = !announceEnabled;

        if (announceEnabled) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new AnnouncementsOnAppend(guid));
            sendToAll(builder);
        } else {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new AnnouncementsOffAppend(guid));
            sendToAll(builder);
        }

        isDirty = true;
    }

    public final void say(ObjectGuid guid, String what, Language lang) {
        if (StringUtil.isEmpty(what)) {
            return;
        }

        // TODO: Add proper RBAC check
        if (WorldConfig.getBoolValue(WorldCfg.AllowTwoSideInteractionChannel)) {
            lang = language.Universal;
        }

        if (!isOn(guid)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new NotMemberAppend());
            sendToOne(builder, guid);

            return;
        }

        var playerInfo = playersStore.get(guid);

        if (playerInfo.isMuted()) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new MutedAppend());
            sendToOne(builder, guid);

            return;
        }

        var player = global.getObjAccessor().findConnectedPlayer(guid);
        sendToAll(new ChannelSayBuilder(this, lang, what, guid, channelGuid), !playerInfo.isModerator() ? guid : ObjectGuid.Empty, !playerInfo.isModerator() && player ? player.getSession().getAccountGUID() : ObjectGuid.Empty);
    }

    public final void addonSay(ObjectGuid guid, String prefix, String what, boolean isLogged) {
        if (what.isEmpty()) {
            return;
        }

        if (!isOn(guid)) {
            NotMemberAppend appender = new NotMemberAppend();
            ChannelNameBuilder builder = new ChannelNameBuilder(this, appender);
            sendToOne(builder, guid);

            return;
        }

        var playerInfo = playersStore.get(guid);

        if (playerInfo.isMuted()) {
            MutedAppend appender = new MutedAppend();
            ChannelNameBuilder builder = new ChannelNameBuilder(this, appender);
            sendToOne(builder, guid);

            return;
        }

        var player = global.getObjAccessor().findConnectedPlayer(guid);

        sendToAllWithAddon(new ChannelWhisperBuilder(this, isLogged ? language.AddonLogged : language.Addon, what, prefix, guid), prefix, !playerInfo.isModerator() ? guid : ObjectGuid.Empty, !playerInfo.isModerator() && player ? player.getSession().getAccountGUID() : ObjectGuid.Empty);
    }

    public final void invite(Player player, String newname) {
        var guid = player.getGUID();

        if (!isOn(guid)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new NotMemberAppend());
            sendToOne(builder, guid);

            return;
        }

        var newp = global.getObjAccessor().FindPlayerByName(newname);

        if (!newp || !newp.isGMVisible()) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new PlayerNotFoundAppend(newname));
            sendToOne(builder, guid);

            return;
        }

        if (isBanned(newp.getGUID())) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new PlayerInviteBannedAppend(newname));
            sendToOne(builder, guid);

            return;
        }

        if (newp.getTeam() != player.getTeam() && (!player.getSession().hasPermission(RBACPermissions.TwoSideInteractionChannel) || !newp.getSession().hasPermission(RBACPermissions.TwoSideInteractionChannel))) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new InviteWrongFactionAppend());
            sendToOne(builder, guid);

            return;
        }

        if (isOn(newp.getGUID())) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new PlayerAlreadyMemberAppend(newp.getGUID()));
            sendToOne(builder, guid);

            return;
        }

        if (!newp.getSocial().hasIgnore(guid, player.getSession().getAccountGUID())) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new InviteAppend(guid));
            sendToOne(builder, newp.getGUID());
        }

        ChannelNameBuilder builder1 = new ChannelNameBuilder(this, new PlayerInvitedAppend(newp.getName()));
        sendToOne(builder1, guid);
    }


    public final void setOwner(ObjectGuid guid) {
        setOwner(guid, true);
    }

    public final void setOwner(ObjectGuid guid, boolean exclaim) {
        if (!ownerGuid.isEmpty()) {
            // [] will re-add player after it possible removed
            var playerInfo = playersStore.get(ownerGuid);

            if (playerInfo != null) {
                playerInfo.setOwner(false);
            }
        }

        ownerGuid = guid;

        if (!ownerGuid.isEmpty()) {
            var oldFlag = getPlayerFlags(ownerGuid);
            var playerInfo = playersStore.get(ownerGuid);

            if (playerInfo == null) {
                return;
            }

            playerInfo.setModerator(true);
            playerInfo.setOwner(true);

            ChannelNameBuilder builder = new ChannelNameBuilder(this, new ModeChangeAppend(ownerGuid, oldFlag, getPlayerFlags(ownerGuid)));
            sendToAll(builder);

            if (exclaim) {
                ChannelNameBuilder ownerBuilder = new ChannelNameBuilder(this, new OwnerChangedAppend(ownerGuid));
                sendToAll(ownerBuilder);
            }

            isDirty = true;
        }
    }

    public final void silenceAll(Player player, String name) {
    }

    public final void unsilenceAll(Player player, String name) {
    }

    public final void declineInvite(Player player) {
    }

    public final int getChannelId() {
        return channelId;
    }

    public final boolean isConstant() {
        return channelId != 0;
    }

    public final ObjectGuid getGUID() {
        return channelGuid;
    }

    public final boolean isLFG() {
        return getFlags().hasFlag(channelFlags.Lfg);
    }

    // will be saved to DB on next channel save interval
    public final void setDirty() {
        isDirty = true;
    }

    public final void setPassword(String npassword) {
        channelPassword = npassword;
    }

    public final boolean checkPassword(String password) {
        return channelPassword.isEmpty() || (Objects.equals(channelPassword, password));
    }

    public final int getNumPlayers() {
        return (int) playersStore.size();
    }

    public final ChannelFlags getFlags() {
        return channelFlags;
    }

    public final AreaTableRecord getZoneEntry() {
        return zoneEntry;
    }

    public final void kick(Player player, String badname) {
        kickOrBan(player, badname, false);
    }

    public final void ban(Player player, String badname) {
        kickOrBan(player, badname, true);
    }

    public final void setModerator(Player player, String newname) {
        setMode(player, newname, true, true);
    }

    public final void unsetModerator(Player player, String newname) {
        setMode(player, newname, true, false);
    }

    public final void setMute(Player player, String newname) {
        setMode(player, newname, false, true);
    }

    public final void unsetMute(Player player, String newname) {
        setMode(player, newname, false, false);
    }

    public final void setOwnership(boolean ownership) {
        ownershipEnabled = ownership;
    }

    public final ChannelMemberFlags getPlayerFlags(ObjectGuid guid) {
        var info = playersStore.get(guid);

        return info != null ? info.getFlags() : 0;
    }

    private void kickOrBan(Player player, String badname, boolean ban) {
        var good = player.getGUID();

        if (!isOn(good)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new NotMemberAppend());
            sendToOne(builder, good);

            return;
        }

        var info = playersStore.get(good);

        if (!info.isModerator() && !player.getSession().hasPermission(RBACPermissions.ChangeChannelNotModerator)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new NotModeratorAppend());
            sendToOne(builder, good);

            return;
        }

        var bad = global.getObjAccessor().FindPlayerByName(badname);
        var victim = bad ? bad.getGUID() : ObjectGuid.Empty;

        if (bad == null || victim.isEmpty() || !isOn(victim)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new PlayerNotFoundAppend(badname));
            sendToOne(builder, good);

            return;
        }

        var changeowner = Objects.equals(ownerGuid, victim);

        if (!player.getSession().hasPermission(RBACPermissions.ChangeChannelNotModerator) && changeowner && ObjectGuid.opNotEquals(good, ownerGuid)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new NotOwnerAppend());
            sendToOne(builder, good);

            return;
        }

        if (ban && !isBanned(victim)) {
            bannedStore.add(victim);
            isDirty = true;

            if (!player.getSession().hasPermission(RBACPermissions.SilentlyJoinChannel)) {
                ChannelNameBuilder builder = new ChannelNameBuilder(this, new PlayerBannedAppend(good, victim));
                sendToAll(builder);
            }
        } else if (!player.getSession().hasPermission(RBACPermissions.SilentlyJoinChannel)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new PlayerKickedAppend(good, victim));
            sendToAll(builder);
        }

        playersStore.remove(victim);
        bad.leftChannel(this);

        if (changeowner && ownershipEnabled && !playersStore.isEmpty()) {
            info.setModerator(true);
            setOwner(good);
        }
    }

    private void setMode(Player player, String p2n, boolean mod, boolean set) {
        var guid = player.getGUID();

        if (!isOn(guid)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new NotMemberAppend());
            sendToOne(builder, guid);

            return;
        }

        var info = playersStore.get(guid);

        if (!info.isModerator() && !player.getSession().hasPermission(RBACPermissions.ChangeChannelNotModerator)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new NotModeratorAppend());
            sendToOne(builder, guid);

            return;
        }

        if (Objects.equals(guid, ownerGuid) && Objects.equals(p2n, player.getName()) && mod) {
            return;
        }

        var newp = global.getObjAccessor().FindPlayerByName(p2n);
        var victim = newp ? newp.getGUID() : ObjectGuid.Empty;

        if (newp == null || victim.isEmpty() || !isOn(victim) || (player.getTeam() != newp.getTeam() && (!player.getSession().hasPermission(RBACPermissions.TwoSideInteractionChannel) || !newp.getSession().hasPermission(RBACPermissions.TwoSideInteractionChannel)))) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new PlayerNotFoundAppend(p2n));
            sendToOne(builder, guid);

            return;
        }

        if (Objects.equals(ownerGuid, victim) && ObjectGuid.opNotEquals(ownerGuid, guid)) {
            ChannelNameBuilder builder = new ChannelNameBuilder(this, new NotOwnerAppend());
            sendToOne(builder, guid);

            return;
        }

        if (mod) {
            setModerator(newp.getGUID(), set);
        } else {
            setMute(newp.getGUID(), set);
        }
    }

    private void joinNotify(Player player) {
        var guid = player.getGUID();

        if (isConstant()) {
            sendToAllButOne(new ChannelUserlistAddBuilder(this, guid), guid);
        } else {
            sendToAll(new ChannelUserlistUpdateBuilder(this, guid));
        }
    }

    private void leaveNotify(Player player) {
        var guid = player.getGUID();

        var builder = new ChannelUserlistRemoveBuilder(this, guid);

        if (isConstant()) {
            sendToAllButOne(builder, guid);
        } else {
            sendToAll(builder);
        }
    }

    private void setModerator(ObjectGuid guid, boolean set) {
        if (!isOn(guid)) {
            return;
        }

        var playerInfo = playersStore.get(guid);

        if (playerInfo.isModerator() != set) {
            var oldFlag = playersStore.get(guid).getFlags();
            playerInfo.setModerator(set);

            ChannelNameBuilder builder = new ChannelNameBuilder(this, new ModeChangeAppend(guid, oldFlag, playerInfo.getFlags()));
            sendToAll(builder);
        }
    }

    private void setMute(ObjectGuid guid, boolean set) {
        if (!isOn(guid)) {
            return;
        }

        var playerInfo = playersStore.get(guid);

        if (playerInfo.isMuted() != set) {
            var oldFlag = playersStore.get(guid).getFlags();
            playerInfo.setMuted(set);

            ChannelNameBuilder builder = new ChannelNameBuilder(this, new ModeChangeAppend(guid, oldFlag, playerInfo.getFlags()));
            sendToAll(builder);
        }
    }

    private void sendToAll(MessageBuilder builder, ObjectGuid guid) {
        sendToAll(builder, guid, null);
    }

    private void sendToAll(MessageBuilder builder) {
        sendToAll(builder, null, null);
    }

    private void sendToAll(MessageBuilder builder, ObjectGuid guid, ObjectGuid accountGuid) {
        LocalizedDo localizer = new LocalizedDo(builder);

        for (var pair : playersStore.entrySet()) {
            var player = global.getObjAccessor().findConnectedPlayer(pair.getKey());

            if (player) {
                if (guid.isEmpty() || !player.getSocial().hasIgnore(guid, accountGuid)) {
                    localizer.invoke(player);
                }
            }
        }
    }

    private void sendToAllButOne(MessageBuilder builder, ObjectGuid who) {
        LocalizedDo localizer = new LocalizedDo(builder);

        for (var pair : playersStore.entrySet()) {
            if (ObjectGuid.opNotEquals(pair.getKey(), who)) {
                var player = global.getObjAccessor().findConnectedPlayer(pair.getKey());

                if (player) {
                    localizer.invoke(player);
                }
            }
        }
    }

    private void sendToOne(MessageBuilder builder, ObjectGuid who) {
        LocalizedDo localizer = new LocalizedDo(builder);

        var player = global.getObjAccessor().findConnectedPlayer(who);

        if (player) {
            localizer.invoke(player);
        }
    }

    private void sendToAllWithAddon(MessageBuilder builder, String addonPrefix, ObjectGuid guid) {
        sendToAllWithAddon(builder, addonPrefix, guid, null);
    }

    private void sendToAllWithAddon(MessageBuilder builder, String addonPrefix) {
        sendToAllWithAddon(builder, addonPrefix, null, null);
    }

    private void sendToAllWithAddon(MessageBuilder builder, String addonPrefix, ObjectGuid guid, ObjectGuid accountGuid) {
        LocalizedDo localizer = new LocalizedDo(builder);

        for (var pair : playersStore.entrySet()) {
            var player = global.getObjAccessor().findConnectedPlayer(pair.getKey());

            if (player) {
                if (player.getSession().isAddonRegistered(addonPrefix) && (guid.isEmpty() || !player.getSocial().hasIgnore(guid, accountGuid))) {
                    localizer.invoke(player);
                }
            }
        }
    }

    private boolean isAnnounce() {
        return announceEnabled;
    }

    public final void setAnnounce(boolean announce) {
        announceEnabled = announce;
    }

    private boolean hasFlag(ChannelFlags flag) {
        return channelFlags.hasFlag(flag);
    }

    private boolean isOn(ObjectGuid who) {
        return playersStore.containsKey(who);
    }

    private boolean isBanned(ObjectGuid guid) {
        return bannedStore.contains(guid);
    }

    public static class PlayerInfo {
        private ChannelMemberFlags flags = ChannelMemberFlags.values()[0];
        private boolean invisible;

        public final ChannelMemberFlags getFlags() {
            return flags;
        }

        public final boolean isInvisible() {
            return invisible;
        }

        public final void setInvisible(boolean on) {
            invisible = on;
        }

        public final boolean hasFlag(ChannelMemberFlags flag) {
            return flags.hasFlag(flag);
        }

        public final void setFlag(ChannelMemberFlags flag) {
            flags = ChannelMemberFlags.forValue(flags.getValue() | flag.getValue());
        }

        public final void removeFlag(ChannelMemberFlags flag) {
            flags = ChannelMemberFlags.forValue(flags.getValue() & ~flag.getValue());
        }

        public final boolean isOwner() {
            return hasFlag(ChannelMemberFlags.owner);
        }

        public final void setOwner(boolean state) {
            if (state) {
                setFlag(ChannelMemberFlags.owner);
            } else {
                removeFlag(ChannelMemberFlags.owner);
            }
        }

        public final boolean isModerator() {
            return hasFlag(ChannelMemberFlags.moderator);
        }

        public final void setModerator(boolean state) {
            if (state) {
                setFlag(ChannelMemberFlags.moderator);
            } else {
                removeFlag(ChannelMemberFlags.moderator);
            }
        }

        public final boolean isMuted() {
            return hasFlag(ChannelMemberFlags.Muted);
        }

        public final void setMuted(boolean state) {
            if (state) {
                setFlag(ChannelMemberFlags.Muted);
            } else {
                removeFlag(ChannelMemberFlags.Muted);
            }
        }
    }
}
