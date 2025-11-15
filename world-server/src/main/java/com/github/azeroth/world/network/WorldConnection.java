package com.github.azeroth.world.network;

import com.github.azeroth.net.Connection;
import com.github.azeroth.net.NettyInbound;
import com.github.azeroth.net.NettyOutbound;

import java.time.Instant;

public interface WorldConnection extends Connection {

    long getId();

    ConnectionType getType();

    Instant getLastPingTime();

    void setLastPingTime(Instant now);

    int incrementAndGetOverSpeedPings();

    void setOverSpeedPings(int overSpeedPings);

    byte[] getServerChallenge();

    byte[] getSessionKey();

    byte[] getEncryptKey();

    @Override
    WorldRequest inbound();

    @Override
    WorldResponse outbound();
}
