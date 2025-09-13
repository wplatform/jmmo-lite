package com.github.azeroth.game.networking.packet.spell;


public class ClearCooldown extends ServerPacket {
    public boolean isPet;
    public int spellID;
    public boolean clearOnHold;

    public ClearCooldown() {
        super(ServerOpcode.ClearCooldown);
    }

    @Override
    public void write() {
        this.writeInt32(spellID);
        this.writeBit(clearOnHold);
        this.writeBit(isPet);
        this.flushBits();
    }
}
