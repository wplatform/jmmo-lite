package com.github.azeroth.world.server.traffic;

import com.rainbowland.worldserver.adapter.ChannelSession;
import com.rainbowland.worldserver.adapter.SessionState;
import com.rainbowland.worldserver.constant.Constants;
import com.rainbowland.proto.SendPacketOpcode;
import com.rainbowland.proto.WorldPacketFrame;
import com.rainbowland.utils.SecureUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.Connection;

import java.io.*;

@Slf4j
public class WorldConnectionInitializer extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("A new client{} connected.", ctx.channel().remoteAddress());
        Connection connection = Connection.from(ctx.channel());
        ByteBuf buffer = connection.outbound().alloc().buffer();
        buffer.writeBytes(Constants.SERVER_CONNECTION_HELLO.getBytes(WorldPacketFrame.PROTOCOL_CHARSET));
        buffer.writeByte('\n');
        ctx.writeAndFlush(buffer);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ChannelSession channelSession = ChannelSession.fromChanel(ctx.channel());
        if (channelSession.getState() == SessionState.INITIALIZING) {
            initConnection(ctx, channelSession, (ByteBuf) msg);
            channelSession.setState(SessionState.INITIALIZED);
        }
        // fire next channel read. the one last user the msg will release the byte buffer.
        super.channelRead(ctx, msg);
    }


    private void initConnection(ChannelHandlerContext ctx, ChannelSession channelSession, ByteBuf buf) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(Constants.CLIENT_CONNECTION_HELLO.length());
        int byteValue;
        while ('\n' != (byteValue = buf.readByte())) {
            outputStream.write(byteValue);
        }

        String helloMessage = outputStream.toString(WorldPacketFrame.PROTOCOL_CHARSET);
        if (!Constants.CLIENT_CONNECTION_HELLO.equals(helloMessage)) {
            log.info("The client{} sent a error hello message  {}", ctx.channel().remoteAddress(), helloMessage);
            ctx.close();
            return;
        }

        byte[] serverChallenge = SecureUtils.generateRandomBytes(16);
        byte[] dosChallenge = SecureUtils.generateRandomBytes(32);

        channelSession.setServerChallenge(serverChallenge);

        //16 + 4 * 8 + 1
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeIntLE(16 + 32 + 1);
        buffer.writeBytes(new byte[WorldPacketFrame.TAG_BYTE_LENGTH]);
        buffer.writeShortLE(SendPacketOpcode.SMSG_AUTH_CHALLENGE.value());

        buffer.writeBytes(dosChallenge);
        buffer.writeBytes(serverChallenge);
        buffer.writeByte(1);
        ctx.writeAndFlush(buffer);

    }

}
