package com.github.azeroth.game.text;


import com.github.azeroth.game.chat.MessageBuilder;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.map.PacketSenderOwning;

public class PlayerTextBuilder extends MessageBuilder {
    private final WorldObject source;
    private final WorldObject talker;
    private final Gender gender;
    private final ChatMsg msgType;
    private final byte textGroup;
    private final int textId;
    private final Language language;
    private final WorldObject target;

    public PlayerTextBuilder(WorldObject obj, WorldObject speaker, Gender gender, ChatMsg msgtype, byte textGroup, int id, Language language, WorldObject target) {
        source = obj;
        gender = gender;
        talker = speaker;
        msgType = msgtype;
        textGroup = textGroup;
        textId = id;
        language = language;
        target = target;
    }


    @Override
    public PacketSenderOwning<ChatPkt> invoke() {
        return invoke(locale.enUS);
    }

    @Override
    public PacketSenderOwning<ChatPkt> invoke(Locale loc_idx) {
        var text = global.getCreatureTextMgr().getLocalizedChatString(source.getEntry(), gender, textGroup, textId, loc_idx);
        PacketSenderOwning<ChatPkt> chat = new PacketSenderOwning<ChatPkt>();
        chat.getData().initialize(msgType, language, talker, target, text, 0, "", loc_idx);

        return chat;
    }
}
