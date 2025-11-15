package com.github.azeroth.game.networking.packet.authentication;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;
import com.github.azeroth.utils.SecureUtils;
import io.netty.buffer.Unpooled;
import lombok.RequiredArgsConstructor;


public class ConnectTo extends ServerPacket {
    public long key;
    public ConnectToSerial serial;
    public ConnectPayload payload;
    public byte con;

    public ConnectTo() {
        super(ServerOpCode.SMSG_CONNECT_TO);
        payload = new ConnectPayload();
    }

    @Override
    public void write() {

        WorldPacket whereBuffer = WorldPacket.wrap(Unpooled.buffer());
        whereBuffer.writeInt8(payload.where.type.value);

        switch (payload.where.type) {
            case IPv4:
                whereBuffer.writeBytes(payload.where.IPv4);

                break;
            case IPv6:
                whereBuffer.writeBytes(payload.where.IPv6);

                break;
            case NamedSocket:
                whereBuffer.writeString(payload.where.nameSocket);

                break;
            default:
                break;
        }

        WorldPacket signBuffer = WorldPacket.wrap(Unpooled.buffer(whereBuffer.content().readableBytes() + 6));
        signBuffer.writeBytes(whereBuffer);
        signBuffer.writeInt32(payload.where.type.value);
        signBuffer.writeInt16(payload.port);


        payload.signature = SecureUtils.signWithRsa(signBuffer.content().array());

        this.writeBytes(payload.signature);
        this.writeBytes(whereBuffer);
        this.writeInt16(payload.port);
        this.writeInt32(serial.ordinal());
        this.writeInt8(con);
        this.writeInt64(key);
    }

    @RequiredArgsConstructor
    public enum AddressType {
        IPv4(1),
        IPv6(2),
        NamedSocket(3); // not supported by windows client
        public final int value;
    }

    public static class ConnectPayload {
        public SocketAddress where = new SocketAddress();
        public short port;
        public byte[] signature;
    }

    public final static class SocketAddress {
        public AddressType type = AddressType.values()[0];

        public byte[] IPv4;
        public byte[] IPv6;
        public String nameSocket;

    }
}
