package com.github.azeroth.game.networking.packet.spell;


public class ClearSpellCharges extends ServerPacket {
    public boolean isPet;
    public int category;

    public ClearSpellCharges() {
        super(ServerOpcode.ClearSpellCharges);
    }

    @Override
    public void write() {
        this.writeInt32(category);
        this.writeBit(isPet);
        this.flushBits();
    }
}
