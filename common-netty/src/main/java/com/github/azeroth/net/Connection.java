package com.github.azeroth.net;

import com.github.azeroth.net.server.NettyPipeline;
import io.netty.channel.*;

import java.net.SocketAddress;
import java.util.Objects;
import java.util.function.Consumer;

public interface Connection {



    static Connection from(Channel channel) {
        Objects.requireNonNull(channel, "channel");
        return channel.attr(CommonNetty.CONNECTION).get();
    }

    default  <T extends Connection> T as(Class<T> clazz) {
        Objects.requireNonNull(clazz, "clazz");
        if (clazz.isAssignableFrom(this.getClass())) {
            return clazz.cast(this);
        }
        return null;
    }

    Channel channel();

    default void close() {
        channel().close();
    }


    default void onClose(Runnable runnable) {
        channel().closeFuture().addListener((ChannelFutureListener) future -> runnable.run());
    }


    default SocketAddress remoteAddress() {
        Channel c = channel();
        return c.remoteAddress();
    }

    default SocketAddress localAddress() {
        Channel c = channel();
        return c.localAddress();
    }


    default Connection addHandlerLast(ChannelHandler handler) {
        return addHandlerLast(handler.getClass().getSimpleName(), handler);
    }

    default Connection addHandlerLast(String name, ChannelHandler handler) {
        CommonNetty.addHandlerBeforeReactorEndHandlers(this, name, handler);
        return this;
    }


    default Connection addHandlerFirst(ChannelHandler handler) {
        return addHandlerFirst(handler.getClass().getSimpleName(), handler);
    }


    default Connection addHandlerFirst(String name, ChannelHandler handler) {
        CommonNetty.addHandlerAfterReactorCodecs(this, name, handler);
        return this;
    }


    default Connection bind() {
        channel().attr(CommonNetty.CONNECTION)
                .set(this);
        return this;
    }

    default NettyInbound inbound() {
        return CommonNetty.unavailableInbound(this);
    }



    default Connection onReadIdle(long idleTimeout, Runnable onReadIdle) {
        return removeHandler(NettyPipeline.OnChannelReadIdle)
                .addHandlerFirst(NettyPipeline.OnChannelReadIdle,
                        new CommonNetty.InboundIdleStateHandler(idleTimeout, onReadIdle));
    }

    default void onTerminate(Consumer<? super Connection> consumer) {
        channel().closeFuture().addListener((ChannelFutureListener) future -> consumer.accept(this));
    }

    default Connection onWriteIdle(long idleTimeout, Runnable onWriteIdle) {
        return removeHandler(NettyPipeline.OnChannelWriteIdle)
                .addHandlerFirst(NettyPipeline.OnChannelWriteIdle,
                        new CommonNetty.OutboundIdleStateHandler(idleTimeout, onWriteIdle));
    }

    default NettyOutbound outbound() {
        return CommonNetty.unavailableOutbound(this);
    }


    default boolean rebind(Connection connection) {
        return channel().attr(CommonNetty.CONNECTION)
                .compareAndSet(this, connection);
    }

    default Connection removeHandler(String name) {
        CommonNetty.removeHandler(channel(), name);
        return this;
    }

    default Connection replaceHandler(String name, ChannelHandler handler) {
        CommonNetty.replaceHandler(channel(), name, handler);
        return this;
    }
}
