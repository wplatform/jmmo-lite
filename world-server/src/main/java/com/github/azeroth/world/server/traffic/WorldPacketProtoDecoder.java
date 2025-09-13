package com.github.azeroth.world.server.traffic;

import com.rainbowland.worldserver.adapter.ChannelSession;
import com.rainbowland.worldserver.adapter.SessionState;
import com.rainbowland.worldserver.crypto.AesGcm;
import com.rainbowland.proto.ProtocolException;
import com.rainbowland.proto.RecvPacketOpcode;
import com.rainbowland.proto.RecvWorldPacket;
import com.rainbowland.proto.WorldPacketFrame;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.List;

@Slf4j
public class WorldPacketProtoDecoder extends MessageToMessageDecoder<ByteBuf> {

    private static final int MAX_PACKET_LENGTH = 0x40000;
    private static final int MAX_OPCODE = 0x3FFF;
    private static final int NUM_OPCODE_HANDLERS = (MAX_OPCODE + 1);
    private static final int UNKNOWN_OPCODE = 0xFFFF;
    private static final int NULL_OPCODE = 0xBADD;


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        // the ByteBuf msg will be released by MessageToMessageDecoder.channelRead finally block.
        try {
            WorldPacketFrame packetFrame = getWorldPacketFrame(ctx, msg);
            out.add(packetFrame);
        } catch (Exception e) {
            log.error("Decode the WorldPacketFrame error.", e);
            throw e;
        }

    }

    private WorldPacketFrame getWorldPacketFrame(ChannelHandlerContext ctx, ByteBuf msg) {
        ChannelSession session = ChannelSession.fromChanel(ctx.channel());
        WorldPacketFrame packetFrame = new WorldPacketFrame();
        int length = msg.readIntLE();
        if (length >= MAX_PACKET_LENGTH) {
            throw new ProtocolException(MessageFormat.format("client {0} sent malformed packet (size: {1})", ctx.channel().remoteAddress(), packetFrame.getLength()));
        }
        packetFrame.setLength(length);
        msg.readBytes(packetFrame.getTag());

        byte[] payloadData = new byte[packetFrame.getLength()];
        msg.readBytes(payloadData);

        if (session.getState() == SessionState.AUTHENTICATED) {
            try {
                payloadData = AesGcm.decrypt(session.getSecretKey(), session.generateIV(), payloadData, packetFrame.getTag());
            } catch (Exception e) {
                throw new ProtocolException(MessageFormat.format("client {0} failed to decrypt payloadData (size: {1})", ctx.channel().remoteAddress(), packetFrame.getLength()), e);
            }
        }


        ByteBuf body = Unpooled.wrappedBuffer(payloadData);
        int opcodeVal = body.readUnsignedShortLE();

        if (opcodeVal >= NUM_OPCODE_HANDLERS) {
            ctx.channel().close();
            throw new ProtocolException(MessageFormat.format("client {0} sent wrong opcode (opcode: {1})", ctx.channel().remoteAddress(), Integer.toHexString(opcodeVal)));
        }

        RecvPacketOpcode opcode = RecvPacketOpcode.of(opcodeVal);
        if (opcode == null) {
            throw new ProtocolException(MessageFormat.format("client {0} sent a wrong opcode (opcode: {1})", ctx.channel().remoteAddress(), Integer.toHexString(opcodeVal)));

        }
        RecvWorldPacket payload = RecvPacketOpcode.createPayloadInstance(opcode);
        payload.deserialize(body);
        packetFrame.setPayload(payload);
        return packetFrame;
    }


}
