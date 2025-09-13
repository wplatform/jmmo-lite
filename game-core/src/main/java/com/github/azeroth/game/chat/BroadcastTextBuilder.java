package com.github.azeroth.game.chat;


import com.github.azeroth.game.entity.object.WorldObject;

public class BroadcastTextBuilder extends MessageBuilder {
    private final WorldObject source;
    private final ChatMsg msgType;
    private final int textId;
    private final Gender gender;
    private final WorldObject target;
    private final int achievementId;


    public BroadcastTextBuilder(WorldObject obj, ChatMsg msgtype, int textId, Gender gender, WorldObject target) {
        this(obj, msgtype, textId, gender, target, 0);
    }

    public BroadcastTextBuilder(WorldObject obj, ChatMsg msgtype, int textId, Gender gender) {
        this(obj, msgtype, textId, gender, null, 0);
    }

    public BroadcastTextBuilder(WorldObject obj, ChatMsg msgtype, int textId, Gender gender, WorldObject target, int achievementId) {
        source = obj;
        msgType = msgtype;
        textId = textId;
        gender = gender;
        target = target;
        achievementId = achievementId;
    }


    @Override
    public ChatPacketSender invoke() {
        return invoke(locale.enUS);
    }

    @Override
    public ChatPacketSender invoke(Locale locale) {
        var bct = CliDB.BroadcastTextStorage.get(textId);

        return new ChatPacketSender(msgType, bct != null ? language.forValue(bct.LanguageID) : language.Universal, source, target, bct != null ? global.getDB2Mgr().GetBroadcastTextValue(bct, locale, gender) : "", achievementId, locale);
    }
}
