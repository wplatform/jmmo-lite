package com.github.azeroth.game.networking;

import io.netty.buffer.ByteBuf;

public abstract class ClientPacket extends WorldPacket {

    protected ClientPacket(ByteBuf data) {
        super(data);
    }

    public abstract void read();
}
