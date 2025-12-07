package com.github.azeroth.world.network;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.world.WorldSession;
import com.github.azeroth.net.NettyInbound;
import io.netty.buffer.ByteBuf;

import java.net.InetAddress;

public interface WorldRequest extends NettyInbound {

    WorldSession getSession();

    String getRemoteHost();


    @Override
    default ByteBuf receive() {
        throw new UnsupportedOperationException("WorldRequest does not support receive ByteBuf");
    }
}
