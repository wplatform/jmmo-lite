package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.ServerPacket;

public class SuspendToken extends ServerPacket {
    public int sequenceIndex = 1;
    public int reason = 1;

    public SuspendToken() {
        super(ServerOpcode.SuspendToken);
    }

    @Override
    public void write() {
        this.writeInt32(sequenceIndex);
        this.writeBits(reason, 2);
        this.flushBits();
    }
}
