package com.github.azeroth.game.entity.conversation;

import com.github.azeroth.game.networking.WorldPacket;


public class ConversationLine {
    
//ORIGINAL LINE: public uint ConversationLineID;
    public int conversationLineID;
    
//ORIGINAL LINE: public uint StartTime;
    public int startTime;
    
//ORIGINAL LINE: public uint UiCameraID;
    public int uiCameraID;
    
//ORIGINAL LINE: public byte ActorIndex;
    public byte actorIndex;
    
//ORIGINAL LINE: public byte Flags;
    public byte flags;
    
//ORIGINAL LINE: public byte ChatType;
    public byte chatType;

    public final void writeCreate(WorldPacket data, Conversation owner, Player receiver) {
        data.WriteUInt32(conversationLineID);
        data.WriteUInt32(getViewerStartTime(this, owner, receiver));
        data.WriteUInt32(uiCameraID);
        data.writeInt8(actorIndex);
        data.writeInt8(flags);
        data.writeInt8(chatType);
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Conversation owner, Player receiver) {
        data.WriteUInt32(conversationLineID);
        data.WriteUInt32(getViewerStartTime(this, owner, receiver));
        data.WriteUInt32(uiCameraID);
        data.writeInt8(actorIndex);
        data.writeInt8(flags);
        data.writeInt8(chatType);
    }

    
//ORIGINAL LINE: public uint GetViewerStartTime(ConversationLine conversationLine, Conversation conversation, Player receiver)
    public final int getViewerStartTime(ConversationLine conversationLine, Conversation conversation, Player receiver) {
        var startTime = conversationLine.startTime;
        var locale = receiver.getSession().getSessionDbLocaleIndex();

        var localizedStartTime = conversation.getLineStartTime(locale, (int) conversationLine.conversationLineID);

        if (system.TimeSpan.opNotEquals(localizedStartTime, TimeSpan.Zero)) {

//ORIGINAL LINE: startTime = (uint)localizedStartTime.TotalMilliseconds;
            startTime = (int) localizedStartTime.getTotalMilliseconds();
        }

        return startTime;
    }
}