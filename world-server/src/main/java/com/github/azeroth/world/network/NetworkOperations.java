package com.github.azeroth.world.network;

import com.github.azeroth.auth.dto.AccountInfo;
import com.github.azeroth.auth.dto.AccountType;
import com.github.azeroth.auth.realm.VariantId;
import com.github.azeroth.common.Locale;
import com.github.azeroth.defines.Expansion;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.enums.HighGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.world.WorldSession;
import com.github.azeroth.net.*;
import com.github.azeroth.net.server.ConnectionObserver;
import com.github.azeroth.world.World;
import com.github.azeroth.world.session.WorldServerSession;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Instant;
import java.time.ZoneOffset;


@Getter
@Setter
public class NetworkOperations extends ChannelOperations<WorldRequest, WorldResponse>
        implements WorldConnection, WorldRequest, WorldResponse {

    private ConnectionType type;
    private long id;
    private boolean authenticated;

    private final byte[] serverChallenge = new byte[32];
    private final byte[] sessionKey = new byte[40];
    private final byte[] encryptKey = new byte[32];

    private final World world;
    private Instant lastPingTime;
    private int overSpeedPings;
    private WorldSession session;
    private String remoteHost;



    public int incrementAndGetOverSpeedPings() {
        return ++overSpeedPings;
    }


    public static NetworkOperations get(Channel ch) {
        return Connection.from(ch)
                .as(NetworkOperations.class);
    }

    public NetworkOperations(Channel channel, ConnectionObserver listener, World world) {
        super(channel, listener);
        this.world = world;
    }

    public void initSession(AccountInfo accountInfo, VariantId clientBuildVariant) {
        if(this.session != null) {
            throw new IllegalStateException("Session already initialized");
        }
        this.session = new WorldServerSession(world,
                accountInfo.getAccountId(),
                accountInfo.getAccountName(),
                accountInfo.getBnetAccountId(),
                ObjectGuid.create(HighGuid.BNetAccount, 0, accountInfo.getBnetAccountId()),
                AccountType.indexOf(accountInfo.getSecurityLevel()),
                Expansion.indexOf(accountInfo.getAccountId()),
                Expansion.indexOf(accountInfo.getExpansion()),
                accountInfo.getOs(),
                accountInfo.getClientBuild(),
                clientBuildVariant,
                Locale.indexOf(accountInfo.getLocale()),
                ZoneOffset.ofTotalSeconds(accountInfo.getTimezoneOffset()),
                accountInfo.getRecruiterId());
        this.authenticated = true;
        onClose(()-> world.removeSession(this.session));
    }

    @Override
    public WorldSession getSession() {
        return this.session;
    }


    @Override
    public String getRemoteHost() {
        if(remoteHost == null) {
            SocketAddress socketAddress = remoteAddress();
            if(socketAddress instanceof InetSocketAddress inetSocketAddress) {
                remoteHost = inetSocketAddress.getHostString();
            } else {
                throw new UnsupportedOperationException("The remote address is not type of InetSocketAddress " + remoteAddress());
            }
        }
        return remoteHost;
    }


    @Override
    public ByteBuf receive() {
        throw new UnsupportedOperationException("receive");
    }


    @Override
    public WorldResponse setWorldPacket(ServerPacket packet) {
        if (!channel().isActive()) {
            CommonNetty.safeRelease(packet);
            throw new RuntimeException("Connection has been closed BEFORE send operation");
        }
        channel().writeAndFlush(packet);
        return this;
    }

    @Override
    public WorldRequest inbound() {
        return this;
    }

    @Override
    public WorldResponse outbound() {
        return this;
    }
}
