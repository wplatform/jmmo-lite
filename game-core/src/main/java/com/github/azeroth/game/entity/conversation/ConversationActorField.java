package com.github.azeroth.game.entity.conversation;

import Framework.Constants.*;
import com.github.azeroth.game.networking.WorldPacket;


public class ConversationActorField {
    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint CreatureID;
    public int creatureID;
    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint CreatureDisplayInfoID;
    public int creatureDisplayInfoID;
    public ObjectGuid actorGUID = new ObjectGuid();
    public int id;
    public ConversationActorType type = ConversationActorType.values()[0];
    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint NoActorObject;
    public int noActorObject;

    public final void writeCreate(WorldPacket data, Conversation owner, Player receiver) {
        data.WriteUInt32(creatureID);
        data.WriteUInt32(creatureDisplayInfoID);
        data.writePackedGuid(actorGUID.clone());
        data.WriteInt32(id);
        data.WriteBits(type, 1);
        data.WriteBits(noActorObject, 1);
        data.FlushBits();
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Conversation owner, Player receiver) {
        data.WriteUInt32(creatureID);
        data.WriteUInt32(creatureDisplayInfoID);
        data.writePackedGuid(actorGUID.clone());
        data.WriteInt32(id);
        data.WriteBits(type, 1);
        data.WriteBits(noActorObject, 1);
        data.FlushBits();
    }
}