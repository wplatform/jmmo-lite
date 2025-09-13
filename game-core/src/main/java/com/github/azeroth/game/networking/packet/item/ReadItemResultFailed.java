package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.ServerPacket;

public class ReadItemResultFailed extends ServerPacket {
    public ObjectGuid item = ObjectGuid.EMPTY;
    public byte subcode;
    public int delay;

    public ReadItemResultFailed() {
        super(ServerOpcode.ReadItemResultFailed);
    }

    @Override
    public void write() {
        this.writeGuid(item);
        this.writeInt32(delay);
        this.writeBits(subcode, 2);
        this.flushBits();
    }
}
