package com.github.azeroth.world.server.network;

import com.github.azeroth.game.world.WorldSession;
import com.github.azeroth.net.NettyInbound;

public interface WorldRequest extends NettyInbound {

    WorldSession getSession();


}
