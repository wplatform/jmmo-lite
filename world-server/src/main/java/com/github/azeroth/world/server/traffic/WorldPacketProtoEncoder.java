package com.github.azeroth.world.server.traffic;

import com.rainbowland.proto.RecvWorldPacket;
import com.rainbowland.worldserver.adapter.ChannelSession;
import com.rainbowland.worldserver.adapter.ServerSession;
import com.rainbowland.worldserver.crypto.AesGcm;
import com.rainbowland.proto.SendPacketOpcode;
import com.rainbowland.proto.SendWorldPacket;
import com.rainbowland.proto.WorldPacketFrame;
import com.rainbowland.utils.Compress;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.zip.Adler32;
import java.util.zip.Checksum;

@Slf4j
public class WorldPacketProtoEncoder extends MessageToByteEncoder<WorldPacketFrame> {

    private static final int SEND_BUFFER_INIT_SIZE = 4096;

    private static final int MIN_SIZE_FOR_COMPRESSION = 0x400;


    @Override
    protected void encode(ChannelHandlerContext ctx, WorldPacketFrame frame, ByteBuf out) throws Exception {
        ChannelSession session = ChannelSession.fromChanel(ctx.channel());
        try {
            switch (session.getState()) {
                case INITIALIZING, INITIALIZED, AUTHENTICATING -> encodeNormalPacket(frame, out);
                case AUTHENTICATED -> encodeEncryptPacket(session, frame, out);
            }
        } catch (Exception e) {
            log.error("Encode WorldPacketFrame error.", e);
            throw e;
        } finally {
            session.increaseSerialNumber();
        }
    }

    private void encodeNormalPacket(WorldPacketFrame frame, ByteBuf out) {
        //write packet header.
        out.writeIntLE(0);
        out.writeBytes(frame.getTag());
        //write packet data, opcode and payload.
        SendWorldPacket payload = (SendWorldPacket) frame.getPayload();
        out.writeShortLE(payload.getOpcode().value());
        payload.serialize(out);
        //calculate the packet data length, opcode and payload length.
        int length = out.readableBytes() - WorldPacketFrame.HEADER_LENGTH;
        //Update the first 4byte with the length
        out.setIntLE(0, length);

    }


    private void encodeEncryptPacket(ServerSession session, WorldPacketFrame frame, ByteBuf out) throws Exception {
        SendWorldPacket payload = (SendWorldPacket) frame.getPayload();
        out.writeShort(payload.getOpcode().value());
        payload.serialize(out);


        byte[] payloadData = new byte[out.readableBytes()];
        out.readBytes(payloadData);
        out.clear();

        // update the payloadLength and payloadData with encrypt packet
        payloadData = AesGcm.encrypt(session.getSecretKey(), session.generateIV(), payloadData, frame.getTag());
        if (payloadData.length > MIN_SIZE_FOR_COMPRESSION) {
            Checksum checksum = new Adler32();
            checksum.update(0x9827D8F1);
            checksum.update(payloadData);
            long uncompressAdler = checksum.getValue();

            byte[] compress = Compress.compress(payloadData);

            checksum = new Adler32();
            checksum.update(0x9827D8F1);
            checksum.update(compress);
            long compressAdler = checksum.getValue();

            out.writeShort(SendPacketOpcode.SMSG_COMPRESSED_PACKET.value());
            out.writeInt(payloadData.length);
            out.writeInt((int) uncompressAdler);
            out.writeInt((int) compressAdler);
            out.writeBytes(compress);

            payloadData = new byte[out.readableBytes()];
            out.readBytes(payloadData);
            out.clear();
        }

        //write packet header.
        out.writeInt(payloadData.length);
        out.writeBytes(frame.getTag());
        //write packet data, opcode and payload.
        out.writeBytes(payloadData);

    }


}
