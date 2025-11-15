package com.github.azeroth.game.networking;

import io.netty.buffer.ByteBuf;

public abstract class ClientPacket extends WorldPacket {

    protected ClientPacket(ByteBuf data) {
        super(data);
    }

    public abstract void read();


    public static final class Null extends ClientPacket {

        private Null(ByteBuf data) {
            super(data);
        }

        @Override
        public void read() {

        }
    }
}
