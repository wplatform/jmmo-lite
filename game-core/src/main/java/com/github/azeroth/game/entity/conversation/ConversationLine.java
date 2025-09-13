package com.github.azeroth.game.entity.conversation;

import com.github.azeroth.game.networking.WorldPacket;


public class ConversationLine {
    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint ConversationLineID;
    public int conversationLineID;
    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint StartTime;
    public int startTime;
    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint UiCameraID;
    public int uiCameraID;
    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public byte ActorIndex;
    public byte actorIndex;
    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public byte Flags;
    public byte flags;
    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
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

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint GetViewerStartTime(ConversationLine conversationLine, Conversation conversation, Player receiver)
    public final int getViewerStartTime(ConversationLine conversationLine, Conversation conversation, Player receiver) {
        var startTime = conversationLine.startTime;
        var locale = receiver.getSession().getSessionDbLocaleIndex();

        var localizedStartTime = conversation.getLineStartTime(locale, (int) conversationLine.conversationLineID);

        if (system.TimeSpan.opNotEquals(localizedStartTime, TimeSpan.Zero)) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: startTime = (uint)localizedStartTime.TotalMilliseconds;
            startTime = (int) localizedStartTime.getTotalMilliseconds();
        }

        return startTime;
    }
}