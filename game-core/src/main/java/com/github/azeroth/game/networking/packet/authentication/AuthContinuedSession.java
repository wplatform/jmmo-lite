package com.github.azeroth.game.networking.packet.authentication;

import com.github.azeroth.game.networking.ClientPacket;
import io.netty.buffer.ByteBuf;

public class AuthContinuedSession extends ClientPacket {

    public long dosResponse;

    public long key;

    public byte[] localChallenge = new byte[32];

    public byte[] digest = new byte[24];

    protected AuthContinuedSession(ByteBuf data) {
        super(data);
    }

    @Override
    public void read() {
        dosResponse = this.readUInt64();
        key = this.readUInt64();
        localChallenge = this.readBytes(16);
        digest = this.readBytes(24);
    }
}
