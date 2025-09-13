package com.github.azeroth.game.chat;


import com.github.azeroth.game.map.PacketSenderOwning;
import com.github.azeroth.game.networking.packet.ChatPkt;

class ChannelSayBuilder extends MessageBuilder {
    private final Channel source;
    private final Language lang;
    private final String what;
    private final ObjectGuid guid;
    private final ObjectGuid channelGuid;

    public ChannelSayBuilder(Channel source, Language lang, String what, ObjectGuid guid, ObjectGuid channelGuid) {
        source = source;
        lang = lang;
        what = what;
        guid = guid;
        channelGuid = channelGuid;
    }


    @Override
    public PacketSenderOwning<ChatPkt> invoke() {
        return invoke(locale.enUS);
    }

    @Override
    public PacketSenderOwning<ChatPkt> invoke(Locale locale) {
        var localeIdx = global.getWorldMgr().getAvailableDbcLocale(locale);

        PacketSenderOwning<ChatPkt> packet = new PacketSenderOwning<ChatPkt>();
        var player = global.getObjAccessor().findConnectedPlayer(guid);

        if (player) {
            packet.getData().initialize(ChatMsg.channel, lang, player, player, what, 0, source.getName(localeIdx));
        } else {
            packet.getData().initialize(ChatMsg.channel, lang, null, null, what, 0, source.getName(localeIdx));
            packet.getData().senderGUID = guid;
            packet.getData().targetGUID = guid;
        }

        packet.getData().channelGUID = channelGuid;

        return packet;
    }
}
