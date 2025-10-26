package com.github.azeroth.world.server.traffic;

import com.github.azeroth.common.Assert;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.networking.opcode.ClientOpCode;
import com.github.azeroth.game.networking.opcode.OpCode;
import com.github.azeroth.utils.Checksum;
import com.github.azeroth.utils.Compress;
import com.github.azeroth.world.server.network.WorldConnection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.TooLongFrameException;

import java.util.List;

import static com.github.azeroth.game.networking.opcode.ServerOpCode.SMSG_COMPRESSED_PACKET;

/**
 * WorldProtocolCodec is the codec for world protocol. It is responsible for encoding and decoding WorldPacket.
 * The complete packet frame structure processed by WorldProtocolCodec is as follows:
 * <p>
 * Packet Frame Structure in Detail
 * Size(4 bytes) Total size of the packet body (excluding header)
 * Tag(12 bytes) 12-byte encryption verification tag
 * EncryptedOpcode(4 bytes) Encrypted operation code (stored in packet buffer)
 * Packet Body(Variable) Encrypted data payload (after decryption contains actual packet data)
 * <p>
 * Normal packet frame:
 * +------------------+------------------+------------------+
 * |   PacketHeader   |           Encrypted data            |
 * +------------------+------------------+------------------+
 * |  Size |   Tag    |         Opcode   |   payload data   |
 * +-------+----------+------------------+------------------+
 * <p>
 * Compressed packet frame:
 * +------------------+------------------------+------------------------+------------------+
 * |   PacketHeader   |  SMSG_COMPRESSED_PACKET | CompressedWorldPacket  | Compressed Data |
 * +------------------+------------------------+------------------------+------------------+
 * |  Size |   Tag    |                        | UncompressedSize       |                  |
 * |4 bytes| 12 bytes |         4 bytes        | UncompressedAdler      |                  |
 * |       |          |                        | CompressedAdler        |                  |
 * +-------+----------+------------------------+------------------------+------------------+
 */
public class WorldProtocolCodec extends ByteToMessageCodec<WorldPacket> {


    private static final int PACKET_SIZE_FIELD_LENGTH = 4;
    private static final int PACKET_HEADER_TAG_LENGTH = 12;
    private static final int PACKET_HEADER_LENGTH = PACKET_SIZE_FIELD_LENGTH + PACKET_HEADER_TAG_LENGTH;

    private static final int MIN_SIZE_FOR_COMPRESSION = 0x400;
    private static final int MAX_PACKET_LENGTH = 0x100000;


    private final WorldPacketCrypt crypt = new WorldPacketCrypt();
    private final Checksum checksum = new Checksum();


    @Override
    protected void encode(ChannelHandlerContext ctx, WorldPacket msg, ByteBuf out) {
        //write packet header.
        OpCode opcode = msg.getOpcode();
        Assert.isTrue(opcode.isServerToClient(), "ServerPacket must be sent to client.");
        //write packet data, opcode and payload.
        if (msg instanceof ServerPacket serverPacket) {
            serverPacket.write();
        }

        int uncompressedSize = msg.content().readableBytes() + 4/*opcode*/;
        if (uncompressedSize > MIN_SIZE_FOR_COMPRESSION && crypt.isInitialized()) {

            byte[] uncompressedPayload = new byte[uncompressedSize];
            Unpooled.wrappedBuffer(uncompressedPayload).writeInt(opcode.getValue()).writeBytes(msg.content());
            int uncompressAdler32 = checksum.adler32(0x9827D8F1, uncompressedPayload);

            var worldSettings = WorldConnection.get(ctx.channel()).getSession().getWorldContext().getWorldSettings();
            byte[] compressed = Compress.compress(uncompressedPayload, worldSettings.compression);
            int compressAdler32 = checksum.adler32(0x9827D8F1, compressed);

            opcode = SMSG_COMPRESSED_PACKET;

            //update msg content
            msg.content().clear();
            //write opcode
            msg.writeInt32(opcode.getValue());
            //uncompressed size
            msg.content().writeInt(uncompressedSize);
            //uncompressed adler32
            msg.content().writeInt(uncompressAdler32);
            //compressed adler32
            msg.content().writeInt(compressAdler32);
            //compressed data
            msg.content().writeBytes(compressed);
        } else {
            byte[] uncompressedPayload = new byte[uncompressedSize];
            Unpooled.wrappedBuffer(uncompressedPayload).writeInt(opcode.getValue()).writeBytes(msg.content());
            msg.content().clear();
            msg.writeBytes(uncompressedPayload);
        }

        int size = msg.content().readableBytes();
        byte[] tag = new byte[PACKET_HEADER_TAG_LENGTH];
        byte[] encrypt = crypt.encrypt(msg.content().array(), tag);

        out.writeInt(size);
        out.writeBytes(tag);
        out.writeBytes(encrypt);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Make sure if the packet size field was received.
        if (in.readableBytes() < PACKET_SIZE_FIELD_LENGTH) {
            // The length field was not received yet - return.
            // This method will be invoked again when more packets are
            // received and appended to the buffer.
            return;
        }
        // The length field is in the buffer.
        // Mark the current buffer position before reading the length field
        // because the whole frame might not be in the buffer yet.
        // We will reset the buffer position to the marked position if
        // there's not enough bytes in the buffer.
        in.markReaderIndex();
        int packetLength = in.readInt();
        if (packetLength >= MAX_PACKET_LENGTH) {
            ctx.fireExceptionCaught(new TooLongFrameException(
                    "client %s sent malformed packet (size: %d)".formatted(ctx.channel().remoteAddress(), packetLength)));
            // Drop the connection.
            ctx.channel().close();
            return;
        }
        // frameLength is the length of the packet + header length.
        int frameLength = packetLength + PACKET_HEADER_LENGTH;
        // Make sure if there's enough bytes in the buffer.
        in.resetReaderIndex();
        if (in.readableBytes() < frameLength) {
            // The whole bytes were not received yet - return.
            // This method will be invoked again when more packets are
            // received and appended to the buffer.
            // Reset to the marked position to read the length field again
            // next time.
            return;
        }
        // There's enough bytes in the buffer. Read it.
        // The whole frame include 4 bytes length and 12 bytes tag.
        ByteBuf packet = in.readSlice(frameLength).retain();
        ClientPacket clientPacket = decodeClientToServerPacket(packet);
        // Successfully decoded a frame. Add the decoded frame.
        out.add(clientPacket);
    }


    private ClientPacket decodeClientToServerPacket(ByteBuf byteBuf) {
        try {
            int size = byteBuf.readInt();
            byte[] tag = new byte[PACKET_HEADER_TAG_LENGTH];
            byte[] payload = new byte[size];
            byteBuf.readBytes(tag);
            byteBuf.readBytes(payload);

            byte[] decrypt = crypt.decrypt(payload, tag);
            ByteBuf byteBufDecrypt = Unpooled.wrappedBuffer(decrypt);
            ClientOpCode clientOpCode = ClientOpCode.valueOf(byteBufDecrypt.readInt());
            ClientPacket clientPacket = WorldPacket.newClientToServer(clientOpCode, byteBufDecrypt);
            clientPacket.read();
            return clientPacket;
        } finally {
            byteBuf.release();
        }
    }

}
