package com.github.azeroth.game.chat;


import com.github.azeroth.game.map.PacketSenderOwning;
import com.github.azeroth.game.networking.packet.UserlistUpdate;

class ChannelUserlistUpdateBuilder extends MessageBuilder {
    private final Channel source;
    private final ObjectGuid guid;

    public ChannelUserlistUpdateBuilder(Channel source, ObjectGuid guid) {
        source = source;
        guid = guid;
    }


    @Override
    public PacketSenderOwning<UserlistUpdate> invoke() {
        return invoke(locale.enUS);
    }

    @Override
    public PacketSenderOwning<UserlistUpdate> invoke(Locale locale) {
        var localeIdx = global.getWorldMgr().getAvailableDbcLocale(locale);

        PacketSenderOwning<UserlistUpdate> userlistUpdate = new PacketSenderOwning<UserlistUpdate>();
        userlistUpdate.getData().updatedUserGUID = guid;
        userlistUpdate.getData().channelFlags = source.getFlags();
        userlistUpdate.getData().userFlags = source.getPlayerFlags(guid);
        userlistUpdate.getData().channelID = source.getChannelId();
        userlistUpdate.getData().channelName = source.getName(localeIdx);

        return userlistUpdate;
    }
}
