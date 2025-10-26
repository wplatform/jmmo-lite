package com.github.azeroth.world.server;

import com.github.azeroth.game.networking.opcode.ClientOpCode;
import com.github.azeroth.net.ChannelOperations;
import com.github.azeroth.net.NettyInbound;
import com.github.azeroth.net.NettyOutbound;
import com.github.azeroth.net.router.Router;
import com.github.azeroth.net.server.TcpServer;
import com.github.azeroth.world.server.network.WorldConnection;
import lombok.NoArgsConstructor;

import java.util.EnumMap;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@NoArgsConstructor(staticName = "create")
public class WorldServer extends TcpServer<WorldServer> {

    private final static PacketOpCodeRouter instance = new PacketOpCodeRouter();

    public final static class PacketOpCodeRouter extends Router<PacketOpCodeRouter, NettyInbound, NettyOutbound> {
        private final EnumMap<ClientOpCode, BiConsumer<NettyInbound, NettyOutbound>> handler = new EnumMap<>(ClientOpCode.class);

        @Override
        protected BiConsumer<NettyInbound, NettyOutbound> identity(NettyInbound request) {
            ClientOpCode opCode = request.receiveObject(ClientOpCode.class);
            return handler.get(opCode);
        }

        public PacketOpCodeRouter route(ClientOpCode opCode, BiConsumer<NettyInbound, NettyOutbound> consumer) {
            handler.put(opCode, consumer);
            return this;
        }

    }

    @Override
    protected ChannelOperations.OnSetup onSetup() {
        return (ch, c, msg) -> new WorldConnection(ch, c);
    }

    @Override
    protected WorldServer self() {
        return this;
    }

    public final WorldServer route(Consumer<? super PacketOpCodeRouter> routesBuilder) {
        Objects.requireNonNull(routesBuilder, "routeBuilder");
        routesBuilder.accept(instance);
        return handle(instance);
    }
}
