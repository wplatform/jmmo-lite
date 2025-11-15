
package com.github.azeroth.net;

import com.github.azeroth.net.server.ConnectionObserver;
import com.github.azeroth.net.server.NettyPipeline;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class ChannelOperations<IN extends NettyInbound, OUT extends NettyOutbound>
        implements NettyInbound, NettyOutbound, Connection {

    private static final Logger log = LoggerFactory.getLogger(ChannelOperations.class);


    public static void addReactiveBridge(Channel ch, OnSetup opsFactory, ConnectionObserver listener) {
        requireNonNull(ch, "channel");
        requireNonNull(opsFactory, "opsFactory");
        requireNonNull(listener, "listener");
        ch.pipeline().addLast(NettyPipeline.ReactiveBridge, new ChannelOperationsHandler(opsFactory, listener));
    }


    public static ChannelOperations<?, ?> get(Channel ch) {
        return Connection.from(ch)
                .as(ChannelOperations.class);
    }


    protected Object receivedMessage;
    protected final Channel channel;
    protected final ConnectionObserver listener;

    protected boolean localActive;
    String longId;
    String shortId;


    public ChannelOperations(Channel channel, ConnectionObserver listener) {
        this.channel = requireNonNull(channel, "connection");
        this.listener = requireNonNull(listener, "listener");

    }

    @Override
    public <T extends Connection> T as(Class<T> clazz) {
        if (clazz == ChannelOperations.class) {
            @SuppressWarnings("unchecked")
            T thiz = (T) this;
            return thiz;
        }
        return Connection.super.as(clazz);
    }

    @Override
    public ByteBufAllocator alloc() {
        return channel.alloc();
    }

    @Override
    public NettyInbound inbound() {
        return this;
    }

    @Override
    public NettyOutbound outbound() {
        return this;
    }

    @Override
    public final Channel channel() {
        return channel;
    }

    @Override
    public ChannelOperations<? extends IN, ? extends OUT> withConnection(Consumer<? super Connection> withConnection) {
        requireNonNull(withConnection, "withConnection");
        withConnection.accept(this);
        return this;
    }

    @Override
    public void close() {
        if (log.isDebugEnabled()) {
            log.debug(CommonNetty.format(channel(), "Disposing ChannelOperation from a channel"));
        }
        channel.close();
    }



    @Override
    public <T> T receiveObject(Class<T> clazz) {
        return clazz.cast(receivedMessage);
    }

    @Override
    public ByteBuf receive() {
        return receiveObject(ByteBuf.class);
    }


    @Override
    public NettyOutbound send(ByteBuf dataStream) {
        return sendObject(dataStream);
    }

    @Override
    public NettyOutbound sendObject(Object dataStream) {
        if (!channel().isActive()) {
            CommonNetty.safeRelease(dataStream);
            throw new RuntimeException("Connection has been closed BEFORE send operation");
        }
        channel().writeAndFlush(dataStream);
        return this;
    }

    protected void onInboundNext(ChannelHandlerContext ctx, Object msg) {
        this.receivedMessage = msg;
        listener.onStateChange(this, ConnectionObserver.State.READ);
    }


    protected void onInboundClose() {
        if (receivedMessage != null) {
            CommonNetty.safeRelease(receivedMessage);
        }
        if (log.isTraceEnabled()) {
            log.trace(CommonNetty.format(channel(), "Disposing ChannelOperation from a channel"),
                    new Exception("ChannelOperation terminal stack"));
        }
        listener.onStateChange(this, ConnectionObserver.State.DISCONNECTING);

    }

    protected final void onInboundError(Throwable err) {
        listener.onUncaughtException(this, err);
    }

    protected void onWritabilityChanged() {
    }


    public String asShortText() {
        if (shortId == null) {
            shortId = channel().id().asShortText();
        }

        return shortId;
    }


    @Override
    public String toString() {
        boolean active = channel().isActive();
        if (localActive == active && longId != null) {
            return longId;
        }

        SocketAddress remoteAddress = channel().remoteAddress();
        SocketAddress localAddress = channel().localAddress();
        String shortText = asShortText();
        if (remoteAddress != null) {
            String localAddressStr = String.valueOf(localAddress);
            String remoteAddressStr = String.valueOf(remoteAddress);
            longId = shortText + ", L:" + localAddressStr + (active ? " - " : " ! ") + "R:" + remoteAddressStr;
        } else if (localAddress != null) {
            String localAddressStr = String.valueOf(localAddress);
            longId = shortText +
                    ", L:" +
                    localAddressStr;
        } else {
            longId = shortText;
        }

        localActive = active;
        return longId;
    }

    /**
     * A {@link ChannelOperations} factory.
     */
    @FunctionalInterface
    public interface OnSetup {

        /**
         * Return an empty, no-op factory.
         *
         * @return an empty, no-op factory
         */
        static OnSetup empty() {
            return EMPTY_SETUP;
        }

        /**
         * Create a new {@link ChannelOperations} given a netty channel, a parent {@link
         * ConnectionObserver} and an optional message (nullable).
         *
         * @param listener a {@link ConnectionObserver}
         * @param msg      an optional message
         * @return the new {@link ChannelOperations}
         */
        ChannelOperations<?, ?> create(Channel channel, ConnectionObserver listener, Object msg);

    }


    static final OnSetup EMPTY_SETUP = (c, l, msg) -> null;


}
