package com.github.azeroth.portal.handler;

import bnet.protocol.RpcProto;
import com.github.azeroth.portal.proto.RpcPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcProtocolEncoder extends MessageToByteEncoder<RpcPacket> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcPacket packet, ByteBuf out) {
        RpcProto.Header header = packet.getHeader();
        byte[] headerBytes = header.toByteArray();
        out.writeShort(headerBytes.length);
        out.writeBytes(headerBytes);
        if(packet.getProtoData() != null) {
            out.writeBytes(packet.getProtoData());
        }
    }
}
