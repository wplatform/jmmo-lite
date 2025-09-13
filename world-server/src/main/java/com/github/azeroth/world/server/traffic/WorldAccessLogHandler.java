package com.github.azeroth.world.server.traffic;

import com.rainbowland.proto.WorldPacketFrame;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class WorldAccessLogHandler extends ChannelDuplexHandler {




    private static final String MISSING_SERVICE = "-";
    private static final String MISSING_ADDR = "-";

    static String applyAddress(@Nullable SocketAddress socketAddress) {
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            return inetSocketAddress.getHostString() + ":" + inetSocketAddress.getPort();
        } else {
            return MISSING_ADDR;
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (LOG.isInfoEnabled() && msg instanceof WorldPacketFrame) {
            final WorldPacketFrame request = (WorldPacketFrame) msg;
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (LOG.isInfoEnabled() && msg instanceof WorldPacketFrame) {
            final WorldPacketFrame response = (WorldPacketFrame) msg;
            ctx.write(msg, promise.unvoid())
                    .addListener(future -> {
                        if (future.isSuccess()) {

                        }
                    });
        } else {
            //"FutureReturnValueIgnored" this is deliberate
            ctx.write(msg, promise);
        }

    }
}
