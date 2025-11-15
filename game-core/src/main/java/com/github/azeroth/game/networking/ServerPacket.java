package com.github.azeroth.game.networking;

import com.github.azeroth.game.networking.opcode.ServerOpCode;
import io.netty.buffer.ByteBufAllocator;
import lombok.Getter;

@Getter
public abstract class ServerPacket extends WorldPacket {

    protected ServerPacket(ServerOpCode opcode) {
        super(opcode);
    }

    protected ServerPacket(ServerOpCode opcode, int initialCapacity) {
        super(opcode, ByteBufAllocator.DEFAULT.buffer(initialCapacity));
    }

    public abstract void write();


    public static final class Raw extends ServerPacket {

        Raw(ServerOpCode opcode) {
            super(opcode);
        }

        Raw(ServerOpCode opcode, int initialCapacity) {
            super(opcode, initialCapacity);
        }

        @Override
        public void write() {

        }
    }
}
