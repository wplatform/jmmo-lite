package com.github.azeroth.game.networking.packet.misc;


public class CorpseReclaimDelay extends ServerPacket {
    public int remaining;

    public CorpseReclaimDelay() {
        super(ServerOpcode.CorpseReclaimDelay);
    }

    @Override
    public void write() {
        this.writeInt32(remaining);
    }
}
