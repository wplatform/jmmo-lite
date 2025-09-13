package com.github.azeroth.portal.handler;


import bnet.protocol.RpcProto;
import com.google.protobuf.InvalidProtocolBufferException;
import com.github.azeroth.portal.proto.RpcPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RpcProtocolDecoder extends ByteToMessageDecoder {


    private static final int FRAME_LENGTH_FIELD_LENGTH = 2;


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws InvalidProtocolBufferException {
        // Make sure if the length field was received.
        if (in.readableBytes() < FRAME_LENGTH_FIELD_LENGTH) {
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


        try {
            int headerLength = in.readUnsignedShort();
            // Make sure if there's enough bytes in the buffer.
            if (in.readableBytes() < headerLength) {
                // The whole bytes were not received yet - return.
                // This method will be invoked again when more packets are
                // received and appended to the buffer.
                // Reset to the marked position to read the length field again
                // next time.
                in.resetReaderIndex();
                return;
            }
            byte[] headerData = new byte[headerLength];
            in.readBytes(headerData);
            RpcProto.Header header = RpcProto.Header.parseFrom(headerData);

            int dataLength = header.getSize();
            if (in.readableBytes() < dataLength) {
                // The whole bytes were not received yet - return.
                // This method will be invoked again when more packets are
                // received and appended to the buffer.
                // Reset to the marked position to read the length field again
                // next time.
                in.resetReaderIndex();
                return;
            }

            byte[] messageData = new byte[dataLength];
            in.readBytes(messageData);
            RpcPacket protocol = new RpcPacket(header, messageData);
            out.add(protocol);
        } catch (InvalidProtocolBufferException e) {
            log.error("Decode Rpc Protocol error", e);
            throw e;
        }
    }


}
