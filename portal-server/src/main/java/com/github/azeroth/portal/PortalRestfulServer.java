package com.github.azeroth.portal;

import com.github.azeroth.net.ChannelOperations;
import com.github.azeroth.net.http.HttpOperations;
import com.github.azeroth.net.http.HttpServerRequest;
import com.github.azeroth.net.http.HttpServerResponse;
import com.github.azeroth.net.http.HttpServerRouter;
import com.github.azeroth.net.server.ConnectionObserver;
import com.github.azeroth.net.server.TcpServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.Consumer;


@Slf4j
public class PortalRestfulServer extends TcpServer<PortalRestfulServer> {

    private final HttpServerRouter instance;

    public static PortalRestfulServer create() {
        return new PortalRestfulServer();
    }

    @Override
    protected PortalRestfulServer self() {
        return this;
    }

    private PortalRestfulServer() {
        super(new ServerBootstrap());
        this.instance = new HttpServerRouter();
    }


    public final PortalRestfulServer route(Consumer<HttpServerRouter> routes) {
        Objects.requireNonNull(routes, "routes");
        routes.accept(instance);
        return handle((in, out) -> {
            instance.accept((HttpServerRequest) in, (HttpServerResponse) out);
        });
    }

    @Override
    protected ChannelOperations.OnSetup onSetup() {
        return (ch, c, msg) -> new ServerOperations(ch, c);
    }

    private static final class ServerOperations extends HttpOperations {

        public ServerOperations(Channel channel, ConnectionObserver listener) {
            super(channel, listener);
        }

        @Override
        protected void deserialize(Object content, ByteBuf buffer, Charset charset) {
            
        }

        @Override
        protected <T> T serialize(ByteBuf content, Class<T> clazz, Charset charset) {
            return null;
        }
    }


}
