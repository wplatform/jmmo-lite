package com.github.azeroth.game.chat;


import com.github.azeroth.game.entity.ObjectGuidGenerator;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.packet.ChannelNotify;

import java.util.ArrayList;
import java.util.HashMap;


public class ChannelManager {
    private static final channelManager allianceChannelMgr = new channelManager(Team.ALLIANCE);
    private static final ChannelManager hordeChannelMgr = new channelManager(Team.Horde);

    private final HashMap<String, channel> customChannels = new HashMap<String, channel>();
    private final HashMap<ObjectGuid, channel> channels = new HashMap<ObjectGuid, channel>();
    private final Team team;
    private final ObjectGuidGenerator guidGenerator;

    public channelManager(Team team) {
        team = team;
        guidGenerator = new ObjectGuidGenerator(HighGuid.ChatChannel);
    }

    public static void loadFromDB() {
        if (!WorldConfig.getBoolValue(WorldCfg.PreserveCustomChannels)) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 custom chat channels. Custom channel saving is disabled.");

            return;
        }

        var oldMSTime = System.currentTimeMillis();
        var days = WorldConfig.getUIntValue(WorldCfg.PreserveCustomChannelDuration);

        if (days != 0) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_OLD_CHANNELS);
            stmt.AddValue(0, days * time.Day);
            DB.characters.execute(stmt);
        }

        var result = DB.characters.query("SELECT name, team, announce, ownership, password, bannedList FROM channels");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 custom chat channels. DB table `channels` is empty.");

            return;
        }

        ArrayList < (String name, Team team)>toDelete = new ArrayList<(String name, Team team) > ();
        int count = 0;

        do {
            var dbName = result.<String>Read(0); // may be different - channel names are case insensitive
            var team = Team.forValue(result.<Integer>Read(1));
            var dbAnnounce = result.<Boolean>Read(2);
            var dbOwnership = result.<Boolean>Read(3);
            var dbPass = result.<String>Read(4);
            var dbBanned = result.<String>Read(5);

            var mgr = forTeam(team);

            if (mgr == null) {
                Log.outError(LogFilter.ServerLoading, String.format("Failed to load custom chat channel '%1$s' from database - invalid team %2$s. Deleted.", dbName, team));
                toDelete.add((dbName, team));

                continue;
            }

            var channel = new channel(mgr.createCustomChannelGuid(), dbName, team, dbBanned);
            channel.setAnnounce(dbAnnounce);
            channel.setOwnership(dbOwnership);
            channel.setPassword(dbPass);
            mgr.customChannels.put(dbName, channel);

            ++count;
        } while (result.NextRow());


        for (var(name, team) : toDelete) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHANNEL);
            stmt.AddValue(0, name);
            stmt.AddValue(1, (int) team);
            DB.characters.execute(stmt);
        }

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s custom chat channels in %2$s ms", count, time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public static ChannelManager forTeam(Team team) {
        if (WorldConfig.getBoolValue(WorldCfg.AllowTwoSideInteractionChannel)) {
            return allianceChannelMgr; // cross-faction
        }

        if (team == Team.ALLIANCE) {
            return allianceChannelMgr;
        }

        if (team == Team.Horde) {
            return hordeChannelMgr;
        }

        return null;
    }

    public static Channel getChannelForPlayerByNamePart(String namePart, Player playerSearcher) {
        for (var channel : playerSearcher.getJoinedChannels()) {
            var chanName = channel.getName(playerSearcher.getSession().getSessionDbcLocale());

            if (chanName.toLowerCase().equals(namePart.toLowerCase())) {
                return channel;
            }
        }

        return null;
    }

    public static Channel getChannelForPlayerByGuid(ObjectGuid channelGuid, Player playerSearcher) {
        for (var channel : playerSearcher.getJoinedChannels()) {
            if (Objects.equals(channel.getGUID(), channelGuid)) {
                return channel;
            }
        }

        return null;
    }

    public static void sendNotOnChannelNotify(Player player, String name) {
        ChannelNotify notify = new ChannelNotify();
        notify.type = ChatNotify.NotMemberNotice;
        notify.channel = name;
        player.sendPacket(notify);
    }

    public final void saveToDB() {
        for (var pair : customChannels.entrySet()) {
            pair.getValue().updateChannelInDB();
        }
    }

    public final Channel getSystemChannel(int channelId) {
        return getSystemChannel(channelId, null);
    }

    public final Channel getSystemChannel(int channelId, AreaTableRecord zoneEntry) {
        var channelGuid = createBuiltinChannelGuid(channelId, zoneEntry);
        var currentChannel = channels.get(channelGuid);

        if (currentChannel != null) {
            return currentChannel;
        }

        var newChannel = new channel(channelGuid, channelId, team, zoneEntry);
        channels.put(channelGuid, newChannel);

        return newChannel;
    }

    public final Channel createCustomChannel(String name) {
        if (customChannels.containsKey(name.toLowerCase())) {
            return null;
        }

        Channel newChannel = new channel(createCustomChannelGuid(), name, team);
        newChannel.setDirty();

        customChannels.put(name.toLowerCase(), newChannel);

        return newChannel;
    }

    public final Channel getCustomChannel(String name) {
        return customChannels.get(name.toLowerCase());
    }

    public final Channel getChannel(int channelId, String name, Player player, boolean notify) {
        return getChannel(channelId, name, player, notify, null);
    }

    public final Channel getChannel(int channelId, String name, Player player) {
        return getChannel(channelId, name, player, true, null);
    }

    public final Channel getChannel(int channelId, String name, Player player, boolean notify, AreaTableRecord zoneEntry) {
        Channel result = null;

        if (channelId != 0) // builtin
        {
            var channel = channels.get(createBuiltinChannelGuid(channelId, zoneEntry));

            if (channel != null) {
                result = channel;
            }
        } else // custom
        {
            var channel = customChannels.get(name.toLowerCase());

            if (channel != null) {
                result = channel;
            }
        }

        if (result == null && notify) {
            var channelName = name;
            tangible.RefObject<String> tempRef_channelName = new tangible.RefObject<String>(channelName);
            channel.getChannelName(tempRef_channelName, channelId, player.getSession().getSessionDbcLocale(), zoneEntry);
            channelName = tempRef_channelName.refArgValue;

            sendNotOnChannelNotify(player, channelName);
        }

        return result;
    }

    public final void leftChannel(int channelId, AreaTableRecord zoneEntry) {
        var guid = createBuiltinChannelGuid(channelId, zoneEntry);
        var channel = channels.get(guid);

        if (channel == null) {
            return;
        }

        if (channel.getNumPlayers() == 0) {
            channels.remove(guid);
        }
    }

    private ObjectGuid createCustomChannelGuid() {
        long high = 0;
        high |= (long) HighGuid.ChatChannel.getValue() << 58;
        high |= (long) global.getWorldMgr().getRealmId().index << 42;
        high |= (long) (team == Team.ALLIANCE ? 3 : 5) << 4;

        ObjectGuid channelGuid = ObjectGuid.EMPTY;
        channelGuid.SetRawValue(high, guidGenerator.generate());

        return channelGuid;
    }


    private ObjectGuid createBuiltinChannelGuid(int channelId) {
        return createBuiltinChannelGuid(channelId, null);
    }

    private ObjectGuid createBuiltinChannelGuid(int channelId, AreaTableRecord zoneEntry) {
        var channelEntry = CliDB.ChatChannelsStorage.get(channelId);
        var zoneId = zoneEntry != null ? zoneEntry.Id : 0;

        if (channelEntry.flags.hasFlag(ChannelDBCFlags.global.getValue() | ChannelDBCFlags.CityOnly.getValue())) {
            zoneId = 0;
        }

        long high = 0;
        high |= (long) HighGuid.ChatChannel.getValue() << 58;
        high |= (long) global.getWorldMgr().getRealmId().index << 42;
        high |= 1 << 25; // built-in

        if (channelEntry.flags.hasFlag(ChannelDBCFlags.CityOnly2)) {
            high |= 1 << 24; // trade
        }

        high |= (long) (zoneId) << 10;
        high |= (long) (team == Team.ALLIANCE ? 3 : 5) << 4;

        ObjectGuid channelGuid = ObjectGuid.EMPTY;
        channelGuid.SetRawValue(high, channelId);

        return channelGuid;
    }
}
