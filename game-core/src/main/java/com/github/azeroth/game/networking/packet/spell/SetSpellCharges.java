package com.github.azeroth.game.networking.packet.spell;


public class SetSpellCharges extends ServerPacket {
    public boolean isPet;
    public int category;
    public int nextRecoveryTime;
    public byte consumedCharges;
    public float chargeModRate = 1.0f;

    public setSpellCharges() {
        super(ServerOpcode.SetSpellCharges);
    }

    @Override
    public void write() {
        this.writeInt32(category);
        this.writeInt32(nextRecoveryTime);
        this.writeInt8(consumedCharges);
        this.writeFloat(chargeModRate);
        this.writeBit(isPet);
        this.flushBits();
    }
}
