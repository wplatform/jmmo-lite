package com.github.azeroth.world.network;

import com.github.azeroth.game.world.WorldSession;
import com.github.azeroth.net.NettyInbound;

import java.net.InetAddress;

public interface WorldRequest extends NettyInbound {

    WorldSession getSession();

    String getRemoteHost();

}
