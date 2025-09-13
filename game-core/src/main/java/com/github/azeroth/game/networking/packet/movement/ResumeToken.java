package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.ServerPacket;

public class ResumeToken extends ServerPacket {
    public int sequenceIndex = 1;
    public int reason = 1;

    public ResumeToken() {
        super(ServerOpcode.ResumeToken);
    }

    @Override
    public void write() {
        this.writeInt32(sequenceIndex);
        this.writeBits(reason, 2);
        this.flushBits();
    }
}
