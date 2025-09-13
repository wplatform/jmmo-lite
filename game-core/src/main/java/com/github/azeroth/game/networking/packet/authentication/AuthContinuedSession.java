package com.github.azeroth.game.networking.packet.authentication;

import com.github.azeroth.game.networking.WorldPacket;

class AuthContinuedSession extends ClientPacket {

    public long dosResponse;

    public long key;

    public byte[] localChallenge = new byte[16];

    public byte[] digest = new byte[24];

    public AuthContinuedSession(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        dosResponse = this.readUInt64();
        key = this.readUInt64();
        localChallenge = this.readBytes(16);
        digest = this.readBytes(24);
    }
}
