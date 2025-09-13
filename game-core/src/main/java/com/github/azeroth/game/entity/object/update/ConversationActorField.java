package com.github.azeroth.game.entity.object.update;


import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public class ConversationActorField {
    public int creatureID;
    public int creatureDisplayInfoID;
    public ObjectGuid actorGUID = ObjectGuid.EMPTY;
    public int id;
    public ConversationActortype type = ConversationActorType.values()[0];
    public int noActorObject;

    public final void writeCreate(WorldPacket data, Conversation owner, Player receiver) {
        data.writeInt32(creatureID);
        data.writeInt32(creatureDisplayInfoID);
        data.writeGuid(actorGUID);
        data.writeInt32(id);
        data.writeBits(type, 1);
        data.writeBits(noActorObject, 1);
        data.flushBits();
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Conversation owner, Player receiver) {
        data.writeInt32(creatureID);
        data.writeInt32(creatureDisplayInfoID);
        data.writeGuid(actorGUID);
        data.writeInt32(id);
        data.writeBits(type, 1);
        data.writeBits(noActorObject, 1);
        data.flushBits();
    }
}
