package com.github.azeroth.game.chat;


import com.github.azeroth.game.map.PacketSenderOwning;
import com.github.azeroth.game.networking.packet.UserlistRemove;

class ChannelUserlistRemoveBuilder extends MessageBuilder {
    private final Channel source;
    private final ObjectGuid guid;

    public ChannelUserlistRemoveBuilder(Channel source, ObjectGuid guid) {
        source = source;
        guid = guid;
    }


    @Override
    public PacketSenderOwning<UserlistRemove> invoke() {
        return invoke(locale.enUS);
    }

    @Override
    public PacketSenderOwning<UserlistRemove> invoke(Locale locale) {
        var localeIdx = global.getWorldMgr().getAvailableDbcLocale(locale);

        PacketSenderOwning<UserlistRemove> userlistRemove = new PacketSenderOwning<UserlistRemove>();
        userlistRemove.getData().removedUserGUID = guid;
        userlistRemove.getData().channelFlags = source.getFlags();
        userlistRemove.getData().channelID = source.getChannelId();
        userlistRemove.getData().channelName = source.getName(localeIdx);

        return userlistRemove;
    }
}
