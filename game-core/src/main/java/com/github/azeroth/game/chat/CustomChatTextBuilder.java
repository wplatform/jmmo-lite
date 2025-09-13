package com.github.azeroth.game.chat;


import com.github.azeroth.game.entity.object.WorldObject;

public class CustomChatTextBuilder extends MessageBuilder {
    private final WorldObject source;
    private final ChatMsg msgType;
    private final String text;
    private final Language language;
    private final WorldObject target;


    public CustomChatTextBuilder(WorldObject obj, ChatMsg msgType, String text, Language language) {
        this(obj, msgType, text, language, null);
    }

    public CustomChatTextBuilder(WorldObject obj, ChatMsg msgType, String text) {
        this(obj, msgType, text, language.Universal, null);
    }

    public CustomChatTextBuilder(WorldObject obj, ChatMsg msgType, String text, Language language, WorldObject target) {
        source = obj;
        msgType = msgType;
        text = text;
        language = language;
        target = target;
    }

    @Override
    public ChatPacketSender invoke(Locale locale) {
        return new ChatPacketSender(msgType, language, source, target, text, 0, locale);
    }
}
