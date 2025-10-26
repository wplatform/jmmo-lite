package com.github.azeroth.world.server.network;

import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.world.WorldSession;
import com.github.azeroth.net.ChannelOperations;
import com.github.azeroth.net.Connection;
import com.github.azeroth.net.server.ConnectionObserver;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class WorldConnection extends ChannelOperations<WorldRequest, WorldResponse>
        implements WorldRequest, WorldResponse {

    private static final AttributeKey<WorldSession> WORLD_SESSION = AttributeKey.valueOf("$WORLD_SESSION");

    private ConnectionType type;
    private long key;

    private byte[] serverChallenge;
    private byte[] sessionKey;
    private byte[] encryptKey;


    public static WorldConnection get(Channel ch) {
        return Connection.from(ch)
                .as(WorldConnection.class);
    }


    public WorldConnection(Channel channel, ConnectionObserver listener) {
        super(channel, listener);
    }

    @Override
    public WorldSession getSession() {
        return channel().attr(WORLD_SESSION).get();
    }

    public void sendPacket(WorldPacket packet) {
        channel().write(packet);
    }


}
