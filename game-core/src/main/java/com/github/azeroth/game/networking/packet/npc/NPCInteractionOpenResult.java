package com.github.azeroth.game.networking.packet.npc;


public class NPCInteractionOpenResult extends ServerPacket {
    public ObjectGuid npc = ObjectGuid.EMPTY;
    public PlayerinteractionType interactionType = PlayerInteractionType.values()[0];
    public boolean success = true;

    public NPCInteractionOpenResult() {
        super(ServerOpcode.NpcInteractionOpenResult);
    }

    @Override
    public void write() {
        this.writeGuid(npc);
        this.writeInt32(interactionType.getValue());
        this.writeBit(success);
        this.flushBits();
    }
}
