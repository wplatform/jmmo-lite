package com.github.azeroth.game.chat;


import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.packet.ChatPkt;

public class ChatPacketSender implements IDoWork<Player> {
    private final ChatMsg type;
    private final Language language;
    private final WorldObject sender;
    private final WorldObject receiver;
    private final String text;
    private final int achievementId;
    private final Locale locale;
    // caches
    public ChatPkt untranslatedPacket;
    public ChatPkt translatedPacket;


    public ChatPacketSender(ChatMsg chatType, Language language, WorldObject sender, WorldObject receiver, String message, int achievementId) {
        this(chatType, language, sender, receiver, message, achievementId, locale.enUS);
    }

    public ChatPacketSender(ChatMsg chatType, Language language, WorldObject sender, WorldObject receiver, String message) {
        this(chatType, language, sender, receiver, message, 0, locale.enUS);
    }

    public ChatPacketSender(ChatMsg chatType, Language language, WorldObject sender, WorldObject receiver, String message, int achievementId, Locale locale) {
        type = chatType;
        language = language;
        sender = sender;
        receiver = receiver;
        text = message;
        achievementId = achievementId;
        locale = locale;

        untranslatedPacket = new ChatPkt();
        untranslatedPacket.initialize(type, language, sender, receiver, text, achievementId, "", locale);
        untranslatedPacket.write();
    }

    public final void invoke(Player player) {
        if (language == language.Universal || language == language.Addon || language == language.AddonLogged || player.canUnderstandLanguage(language)) {
            player.sendPacket(untranslatedPacket);

            return;
        }

        if (translatedPacket == null) {
            translatedPacket = new ChatPkt();
            translatedPacket.initialize(type, language, sender, receiver, global.getLanguageMgr().translate(text, (int) language.getValue(), player.getSession().getSessionDbcLocale()), achievementId, "", locale);
            translatedPacket.write();
        }

        player.sendPacket(translatedPacket);
    }
}
