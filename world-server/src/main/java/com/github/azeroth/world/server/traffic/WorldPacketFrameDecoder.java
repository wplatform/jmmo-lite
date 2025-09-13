package com.github.azeroth.world.server.traffic;

import com.rainbowland.proto.WorldPacketFrame;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;


public class WorldPacketFrameDecoder extends ByteToMessageDecoder {


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // Make sure if the length field was received.
        if (in.readableBytes() < WorldPacketFrame.FRAME_LENGTH_FIELD_LENGTH) {
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
        // should include header 12 byte tag.
        int frameLength = in.readIntLE() + WorldPacketFrame.TAG_BYTE_LENGTH;
        // Make sure if there's enough bytes in the buffer.
        if (in.readableBytes() < frameLength) {
            // The whole bytes were not received yet - return.
            // This method will be invoked again when more packets are
            // received and appended to the buffer.
            // Reset to the marked position to read the length field again
            // next time.
            in.resetReaderIndex();
            return;
        }
        // There's enough bytes in the buffer. Read it.
        // The whole frame include 4 bytes length and 12 bytes tag.
        ByteBuf frame = in.resetReaderIndex()
                .readSlice(frameLength + WorldPacketFrame.FRAME_LENGTH_FIELD_LENGTH).retain();
        // Successfully decoded a frame. Add the decoded frame.
        out.add(frame);
    }
}
