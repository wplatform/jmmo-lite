package com.github.azeroth.game.networking.packet.misc;


public class TriggerCinematic extends ServerPacket {

    public int cinematicID;
    public ObjectGuid conversationGuid = ObjectGuid.EMPTY;

    public TriggerCinematic() {
        super(ServerOpcode.TriggerCinematic);
    }

    @Override
    public void write() {
        this.writeInt32(cinematicID);
        this.writeGuid(conversationGuid);
    }
}
