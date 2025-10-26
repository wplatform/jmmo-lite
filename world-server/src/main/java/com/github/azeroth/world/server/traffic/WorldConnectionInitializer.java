package com.github.azeroth.world.server.traffic;


import com.github.azeroth.crypto.CryptoDefine;
import com.github.azeroth.game.networking.packet.authentication.AuthChallenge;
import com.github.azeroth.utils.RandomUtil;
import com.github.azeroth.world.server.network.WorldConnection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.TooLongFrameException;

import java.nio.charset.StandardCharsets;

public class WorldConnectionInitializer extends ChannelInboundHandlerAdapter {


    private static final String SERVER_CONNECTION_INITIALIZER = "WORLD OF WARCRAFT CONNECTION - SERVER TO CLIENT - V2\n";
    private static final String CLIENT_CONNECTION_INITIALIZER = "WORLD OF WARCRAFT CONNECTION - CLIENT TO SERVER - V2\n";

    private static final String INITIALIZER_DECODER = "lineBasedFrameDecoder";

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.pipeline().addFirst(INITIALIZER_DECODER, new LineBasedFrameDecoder(100, false, true));
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeBytes(SERVER_CONNECTION_INITIALIZER.getBytes());
        ctx.writeAndFlush(buffer);
        //fire NettyPipeline.ReactiveBridge to let the client know that the connection is active.
        ctx.fireChannelActive();
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

                ctx.fireExceptionCaught(
                        new TooLongFrameException(
                                "frame length (" + length + ") exceeds the allowed maximum (" + maxLength + ')'));

                ctx.close();
                return;
            }
        } finally {
            buf.release();
        }
        ctx.pipeline().remove(INITIALIZER_DECODER);
        ctx.pipeline().remove(this);
        ctx.pipeline().addFirst(new WorldProtocolCodec());
        sendAuthSession(ctx);
    }


    private void sendAuthSession(ChannelHandlerContext ctx) {
        byte[] serverChallenge = RandomUtil.randomBytes(CryptoDefine.SERVER_CHALLENGE_LENGTH);
        WorldConnection connection = WorldConnection.get(ctx.channel());
        connection.setServerChallenge(serverChallenge);
        AuthChallenge authChallenge = new AuthChallenge();
        authChallenge.challenge = serverChallenge;
        authChallenge.dosChallenge = RandomUtil.randomBytes(32);
        authChallenge.dosZeroBits = 1;
        ctx.writeAndFlush(authChallenge);
    }

}
