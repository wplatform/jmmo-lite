package com.github.azeroth.game.chat;


import com.github.azeroth.game.map.PacketSenderOwning;
import com.github.azeroth.game.networking.packet.ChannelNotifyJoined;

class ChannelNotifyJoinedBuilder extends MessageBuilder {
    private final Channel source;

    public ChannelNotifyJoinedBuilder(Channel source) {
        source = source;
    }


    @Override
    public PacketSenderOwning<ChannelNotifyJoined> invoke() {
        return invoke(locale.enUS);
    }

    @Override
    public PacketSenderOwning<ChannelNotifyJoined> invoke(Locale locale) {
        var localeIdx = global.getWorldMgr().getAvailableDbcLocale(locale);

        PacketSenderOwning<ChannelNotifyJoined> notify = new PacketSenderOwning<ChannelNotifyJoined>();
        //notify.channelWelcomeMsg = "";
        notify.getData().chatChannelID = (int) source.getChannelId();
        //notify.instanceID = 0;
        notify.getData().channelFlags = source.getFlags();
        notify.getData().channel = source.getName(localeIdx);
        notify.getData().channelGUID = source.getGUID();

        return notify;
    }
}
