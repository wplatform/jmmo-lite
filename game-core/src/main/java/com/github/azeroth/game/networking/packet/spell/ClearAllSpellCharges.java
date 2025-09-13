package com.github.azeroth.game.networking.packet.spell;


public class ClearAllSpellCharges extends ServerPacket {
    public boolean isPet;

    public ClearAllSpellCharges() {
        super(ServerOpcode.ClearAllSpellCharges);
    }

    @Override
    public void write() {
        this.writeBit(isPet);
        this.flushBits();
    }
}
