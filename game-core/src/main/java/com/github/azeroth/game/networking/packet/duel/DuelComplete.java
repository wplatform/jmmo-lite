package com.github.azeroth.game.networking.packet.duel;


public class DuelComplete extends ServerPacket {
    public boolean started;

    public duelComplete() {
        super(ServerOpcode.DuelComplete);
    }

    @Override
    public void write() {
        this.writeBit(started);
        this.flushBits();
    }
}
