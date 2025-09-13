package com.github.azeroth.game.chat;


import com.github.azeroth.game.map.PacketSenderOwning;
import com.github.azeroth.game.networking.packet.ChannelNotifyLeft;

class ChannelNotifyLeftBuilder extends MessageBuilder {
    private final Channel source;
    private final boolean suspended;

    public ChannelNotifyLeftBuilder(Channel source, boolean suspend) {
        source = source;
        suspended = suspend;
    }


    @Override
    public PacketSenderOwning<ChannelNotifyLeft> invoke() {
        return invoke(locale.enUS);
    }

    @Override
    public PacketSenderOwning<ChannelNotifyLeft> invoke(Locale locale) {
        var localeIdx = global.getWorldMgr().getAvailableDbcLocale(locale);

        PacketSenderOwning<ChannelNotifyLeft> notify = new PacketSenderOwning<ChannelNotifyLeft>();
        notify.getData().channel = source.getName(localeIdx);
        notify.getData().chatChannelID = source.getChannelId();
        notify.getData().suspended = suspended;

        return notify;
    }
}
