package com.github.azeroth.game.text;


import com.github.azeroth.defines.ChatMsg;
import com.github.azeroth.defines.Gender;
import com.github.azeroth.defines.Language;
import com.github.azeroth.game.chat.ChatPacketSender;
import com.github.azeroth.game.chat.MessageBuilder;
import com.github.azeroth.game.entity.object.WorldObject;

public class CreatureTextBuilder extends MessageBuilder {
    private final WorldObject source;
    private final Gender gender;
    private final ChatMsg msgType;

    private final byte textGroup;

    private final int textId;
    private final Language language;
    private final WorldObject target;


    public CreatureTextBuilder(WorldObject obj, Gender gender, ChatMsg msgtype, byte textGroup, int id, Language language, WorldObject target) {
        source = obj;
        gender = gender;
        msgType = msgtype;
        textGroup = textGroup;
        textId = id;
        language = language;
        target = target;
    }


    @Override
    public ChatPacketSender invoke() {
        return invoke(locale.enUS);
    }

    @Override
    public ChatPacketSender invoke(Locale locale) {
        var text = global.getCreatureTextMgr().getLocalizedChatString(source.getEntry(), gender, textGroup, textId, locale);

        return new ChatPacketSender(msgType, language, source, target, text, 0, locale);
    }
}
