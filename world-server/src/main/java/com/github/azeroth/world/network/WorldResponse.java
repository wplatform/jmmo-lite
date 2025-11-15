package com.github.azeroth.world.network;

import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.net.NettyOutbound;
import io.netty.buffer.ByteBuf;

public interface WorldResponse extends NettyOutbound {

    @Override
    default NettyOutbound sendObject(Object message) {
        throw new UnsupportedOperationException();
    }

    @Override
    default NettyOutbound send(ByteBuf message) {
        throw new UnsupportedOperationException();
    }

    WorldResponse setWorldPacket(ServerPacket packet);

}
