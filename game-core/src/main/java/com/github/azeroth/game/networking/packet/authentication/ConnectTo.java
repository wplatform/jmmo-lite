package com.github.azeroth.game.networking.packet.authentication;


import Framework.Cryptography.*;
import com.github.azeroth.game.networking.ServerPacket;

public class ConnectTo extends ServerPacket {
    public long key;
    public ConnectToserial serial = ConnectToSerial.values()[0];
    public Connectpayload payload;
    public byte con;
    public ConnectTo() {
        super(ServerOpcode.ConnectTo);
        payload = new ConnectPayload();
    }

    @Override
    public void write() {
        ByteBuffer whereBuffer = new byteBuffer();
        whereBuffer.writeInt8((byte) payload.where.type.getValue());

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

        Sha256 hash = new Sha256();
        hash.process(whereBuffer.getData(), (int) whereBuffer.getSize());
        hash.process((int) payload.where.type.getValue());
        hash.finish(BitConverter.GetBytes(payload.port));

        payload.signature = RsaCrypt.RSA.SignHash(hash.digest, HashAlgorithmName.SHA256, RSASignaturePadding.Pkcs1).reverse().ToArray();

        this.writeBytes(payload.signature, (int) payload.signature.length);
        this.writeBytes(whereBuffer);
        this.writeInt16(payload.port);
        this.writeInt32((int) serial.getValue());
        this.writeInt8(con);
        this.writeInt64(key);
    }

    public enum AddressType {
        IPv4(1),
        IPv6(2),
        NamedSocket(3); // not supported by windows client

        public static final int SIZE = Integer.SIZE;
        private static java.util.HashMap<Integer, AddressType> mappings;
        private int intValue;

        private AddressType(int value) {
            intValue = value;
            getMappings().put(value, this);
        }

        private static java.util.HashMap<Integer, AddressType> getMappings() {
            if (mappings == null) {
                synchronized (AddressType.class) {
                    if (mappings == null) {
                        mappings = new java.util.HashMap<Integer, AddressType>();
                    }
                }
            }
            return mappings;
        }

        public static AddressType forValue(int value) {
            return getMappings().get(value);
        }

        public int getValue() {
            return intValue;
        }
    }

    public static class ConnectPayload {
        public socketAddress where = new socketAddress();
        public short port;
        public byte[] signature = new byte[256];
    }

    public final static class SocketAddress {
        public Addresstype type = AddressType.values()[0];

        public byte[] IPv4;
        public byte[] IPv6;
        public String nameSocket;

        public SocketAddress clone() {
            SocketAddress varCopy = new socketAddress();

            varCopy.type = this.type;
            varCopy.IPv4 = this.IPv4;
            varCopy.IPv6 = this.IPv6;
            varCopy.nameSocket = this.nameSocket;

            return varCopy;
        }
    }
}
