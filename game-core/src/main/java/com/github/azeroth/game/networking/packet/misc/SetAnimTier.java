package com.github.azeroth.game.networking.packet.misc;


public class SetAnimTier extends ServerPacket {
    public ObjectGuid unit = ObjectGuid.EMPTY;
    public int tier;

    public setAnimTier() {
        super(ServerOpcode.SetAnimTier);
    }

    @Override
    public void write() {
        this.writeGuid(unit);
        this.writeBits(tier, 3);
        this.flushBits();
    }
}
