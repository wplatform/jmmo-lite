package com.github.azeroth.game.chat;


import com.github.azeroth.game.map.PacketSenderOwning;
import com.github.azeroth.game.networking.packet.ChatPkt;

class ChannelWhisperBuilder extends MessageBuilder {
    private final Channel source;
    private final Language lang;
    private final String what;
    private final String prefix;
    private final ObjectGuid guid;

    public ChannelWhisperBuilder(Channel source, Language lang, String what, String prefix, ObjectGuid guid) {
        source = source;
        lang = lang;
        what = what;
        prefix = prefix;
        guid = guid;
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
            packet.getData().initialize(ChatMsg.channel, lang, player, player, what, 0, source.getName(localeIdx), locale.enUS, prefix);
        } else {
            packet.getData().initialize(ChatMsg.channel, lang, null, null, what, 0, source.getName(localeIdx), locale.enUS, prefix);
            packet.getData().senderGUID = guid;
            packet.getData().targetGUID = guid;
        }

        return packet;
    }
}
