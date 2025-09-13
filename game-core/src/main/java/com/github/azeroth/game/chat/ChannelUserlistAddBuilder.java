package com.github.azeroth.game.chat;


import com.github.azeroth.game.map.PacketSenderOwning;
import com.github.azeroth.game.networking.packet.UserlistAdd;

class ChannelUserlistAddBuilder extends MessageBuilder {
    private final Channel source;
    private final ObjectGuid guid;

    public ChannelUserlistAddBuilder(Channel source, ObjectGuid guid) {
        source = source;
        guid = guid;
    }


    @Override
    public PacketSenderOwning<UserlistAdd> invoke() {
        return invoke(locale.enUS);
    }

    @Override
    public PacketSenderOwning<UserlistAdd> invoke(Locale locale) {
        var localeIdx = global.getWorldMgr().getAvailableDbcLocale(locale);

        PacketSenderOwning<UserlistAdd> userlistAdd = new PacketSenderOwning<UserlistAdd>();
        userlistAdd.getData().addedUserGUID = guid;
        userlistAdd.getData().channelFlags = source.getFlags();
        userlistAdd.getData().userFlags = source.getPlayerFlags(guid);
        userlistAdd.getData().channelID = source.getChannelId();
        userlistAdd.getData().channelName = source.getName(localeIdx);

        return userlistAdd;
    }
}
