package com.github.azeroth.game.networking.packet.npc;


public class GossipComplete extends ServerPacket {
    public boolean suppressSound;

    public GossipComplete() {
        super(ServerOpcode.GossipComplete);
    }

    @Override
    public void write() {
        this.writeBit(suppressSound);
        this.flushBits();
    }
}
