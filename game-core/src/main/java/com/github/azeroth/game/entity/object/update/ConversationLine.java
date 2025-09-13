package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public class ConversationLine {
    public int conversationLineID;
    public int startTime;
    public int uiCameraID;
    public byte actorIndex;
    public byte flags;
    public byte chatType;

    public final void writeCreate(WorldPacket data, Conversation owner, Player receiver) {
        data.writeInt32(conversationLineID);
        data.writeInt32(getViewerStartTime(this, owner, receiver));
        data.writeInt32(uiCameraID);
        data.writeInt8(actorIndex);
        data.writeInt8(flags);
        data.writeInt8(chatType);
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Conversation owner, Player receiver) {
        data.writeInt32(conversationLineID);
        data.writeInt32(getViewerStartTime(this, owner, receiver));
        data.writeInt32(uiCameraID);
        data.writeInt8(actorIndex);
        data.writeInt8(flags);
        data.writeInt8(chatType);
    }

    public final int getViewerStartTime(ConversationLine conversationLine, Conversation conversation, Player receiver) {
        var startTime = conversationLine.startTime;
        var locale = receiver.getSession().getSessionDbLocaleIndex();

        var localizedStartTime = conversation.GetLineStartTime(locale, (int) conversationLine.conversationLineID);

        if (duration.opNotEquals(localizedStartTime, duration.Zero)) {
            startTime = (int) localizedStartTime.TotalMilliseconds;
        }

        return startTime;
    }
}
