package com.github.azeroth.game.chat;


import com.github.azeroth.game.map.PacketSenderOwning;
import com.github.azeroth.game.networking.packet.ChannelNotify;

// initial packet data (notify type and channel name)
class ChannelNameBuilder extends MessageBuilder {
    private final Channel source;
    private final IChannelAppender modifier;

    public ChannelNameBuilder(Channel source, IChannelAppender modifier) {
        source = source;
        modifier = modifier;
    }


    @Override
    public PacketSenderOwning<ChannelNotify> invoke() {
        return invoke(locale.enUS);
    }

    @Override
    public PacketSenderOwning<ChannelNotify> invoke(Locale locale) {
        // LocalizedPacketDo sends client DBC locale, we need to get available to server locale
        var localeIdx = global.getWorldMgr().getAvailableDbcLocale(locale);

        PacketSenderOwning<ChannelNotify> sender = new PacketSenderOwning<ChannelNotify>();
        sender.getData().type = modifier.getNotificationType();
        sender.getData().channel = source.getName(localeIdx);
        modifier.append(sender.getData());
        sender.getData().write();

        return sender;
    }
}

//Appenders

