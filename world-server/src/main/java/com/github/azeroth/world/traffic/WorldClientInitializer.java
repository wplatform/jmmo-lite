package com.github.azeroth.world.traffic;


import com.github.azeroth.game.networking.packet.authentication.AuthChallenge;
import com.github.azeroth.net.Connection;
import com.github.azeroth.net.server.NettyPipeline;
import com.github.azeroth.utils.RandomUtil;
import com.github.azeroth.world.network.WorldConnection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.LineBasedFrameDecoder;

import java.nio.charset.StandardCharsets;

public class WorldClientInitializer extends ChannelInboundHandlerAdapter {


    private static final String SERVER_CONNECTION_INITIALIZER = "WORLD OF WARCRAFT CONNECTION - SERVER TO CLIENT - V2\n";
    private static final String CLIENT_CONNECTION_INITIALIZER = "WORLD OF WARCRAFT CONNECTION - CLIENT TO SERVER - V2\n";

    private static final String INITIALIZER_DECODER = "lineBasedFrameDecoder";

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //fire NettyPipeline.ReactiveBridge to let the client know that the connection is active.
        ctx.fireChannelActive();
        Connection connection = Connection.from(ctx.channel());
        connection.addHandlerFirst(INITIALIZER_DECODER, new LineBasedFrameDecoder(100, false, true));
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeBytes(SERVER_CONNECTION_INITIALIZER.getBytes());
        ctx.writeAndFlush(buffer);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        try {
            String bodyMessage = buf.toString(StandardCharsets.UTF_8);
            if (!CLIENT_CONNECTION_INITIALIZER.equals(bodyMessage)) {
                ctx.fireExceptionCaught(new IllegalStateException("Client " + ctx.channel().remoteAddress() + " send invalid initializer:" + bodyMessage));
                ctx.close();
                return;
            }
        } finally {
            buf.release();
        }
        Connection connection = Connection.from(ctx.channel());
        connection.removeHandler(INITIALIZER_DECODER);
        connection.removeHandler(NettyPipeline.WorldClientInitializer);
        connection.addHandlerFirst(NettyPipeline.WorldProtocolCodec, new WorldProtocolCodec());
        sendAuthSession(ctx);
    }


    private void sendAuthSession(ChannelHandlerContext ctx) {
        WorldConnection connection = Connection.from(ctx.channel()).as(WorldConnection.class);
        byte[] serverChallenge = RandomUtil.randomBytes(connection.getServerChallenge());
        AuthChallenge authChallenge = new AuthChallenge();
        authChallenge.challenge = serverChallenge;
        authChallenge.dosChallenge = RandomUtil.randomBytes(32);
        authChallenge.dosZeroBits = 1;
        ctx.writeAndFlush(authChallenge);
    }

}
