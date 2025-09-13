package com.github.azeroth.game.networking;


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.exeception.ValueOverflowException;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.networking.opcode.OpCode;
import com.github.azeroth.game.networking.opcode.ServerOpCode;
import com.github.azeroth.utils.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.DefaultByteBufHolder;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class WorldPacket extends DefaultByteBufHolder {

    @Getter
    private OpCode opcode;
    // only set for a specific set of opcodes, for performance reasons.
    private LocalDateTime m_receivedTime = LocalDateTime.MIN;

    private byte bitPosition = 8;
    private byte bitValue;

    protected WorldPacket(ByteBuf data) {
        super(data);
    }

    protected WorldPacket(ServerOpCode opcode) {
        super(ByteBufAllocator.DEFAULT.buffer());
        this.opcode = opcode;
    }

    protected WorldPacket(ServerOpCode opcode, int estimatedSize) {
        super(ByteBufAllocator.DEFAULT.buffer(estimatedSize));
        this.opcode = opcode;
    }

    protected WorldPacket(ServerOpCode opcode, ByteBuf data) {
        super(data);
        this.opcode = opcode;
    }

    public static WorldPacket newServerToClient(ServerOpCode opcode) {
        return new WorldPacket(opcode);
    }

    public static WorldPacket newServerToClient(ServerOpCode opcode, ByteBuf byteBuf) {
        return new WorldPacket(opcode, byteBuf);
    }

    public static WorldPacket newServerToClient(ServerOpCode opcode, int estimatedSize) {
        return new WorldPacket(opcode, estimatedSize);
    }

    public static WorldPacket newClientToServer(ByteBuf byteBuf) {
        return new WorldPacket(byteBuf);
    }

    public static WorldPacket wrap(ByteBuf byteBuf) {
        return new WorldPacket(byteBuf);
    }

    public final ObjectGuid readPackedGuid() {
        byte loLength = content().readByte();
        byte hiLength = content().readByte();
        long low = readVarUInt64(loLength);
        long high = readVarUInt64(hiLength);
        return new ObjectGuid(high, low);
    }

    public final boolean readBoolean() {
        resetBitPos();
        return content().readBoolean();
    }

    public final byte readByte() {
        resetBitPos();
        return content().readByte();
    }

    public final short readUByte() {
        resetBitPos();
        return content().readUnsignedByte();
    }

    public final short readShort() {
        resetBitPos();
        return content().readShort();
    }

    public final int readUShort() {
        resetBitPos();
        return content().readUnsignedShort();
    }

    public final int readInt() {
        resetBitPos();
        return content().readInt();
    }


    public final long readLong() {
        resetBitPos();
        return content().readLong();
    }


    public final short readInt16() {
        resetBitPos();
        return content().readShort();
    }

    public final int readInt32() {
        resetBitPos();
        return content().readInt();
    }

    public final long readInt64() {
        resetBitPos();
        return content().readLong();
    }

    public final short readUInt8() {
        resetBitPos();
        return content().readUnsignedByte();
    }

    public final int readUInt16() {
        resetBitPos();
        return content().readUnsignedShort();
    }

    public final int readUInt32() {
        resetBitPos();
        int i = content().readInt();
        if (i < 0) {
            throw new ValueOverflowException("readUInt " + i + " overflowed.");
        }
        return i;
    }

    public final long readUInt64() {
        resetBitPos();
        long l = content().readLong();
        if (l < 0) {
            throw new ValueOverflowException("ReadUInt64 " + l + " overflowed.");
        }
        return l;
    }

    public final float readFloat() {
        resetBitPos();
        return content().readFloat();
    }

    public final double readDouble() {
        resetBitPos();
        return content().readDouble();
    }

    public final String readCString() {
        resetBitPos();
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        while (true) {
            byte b = content().readByte();
            if (b == '\0') {
                break;
            }
            data.write(b);
        }
        return data.toString(StandardCharsets.UTF_8);
    }

    public final String readString(int length) {
        if (length == 0) {
            return "";
        }
        return new String(readBytes(length), StandardCharsets.UTF_8);
    }


    public final byte[] readBytes(int length) {
        resetBitPos();
        byte[] bytes = new byte[length];
        content().readBytes(bytes);
        return bytes;
    }

    public int readerIndex() {
        return content().readerIndex();
    }

    public ByteBuf readerIndex(int readerIndex) {
        resetBitPos();
        return content().readerIndex(readerIndex);
    }

    public final int writerIndex() {
        return content().writerIndex();
    }

    public final ByteBuf writerIndex(int writerIndex) {
        flushBits();
        return content().writerIndex(writerIndex);
    }


    public final WorldPacket slice(int index, int length) {
        ByteBuf slice = content().slice(index, length);
        return WorldPacket.wrap(slice);
    }


    public final void capacity(int newCapacity) {
        content().capacity(newCapacity);
    }

    public final Instant readPackedTime() {
        int int32 = readInt32();

        LocalDateTime dateTime = LocalDateTime.of(((int32 >> 24) & 0x1F) + 2000,
                ((int32 >> 20) & 0xF) + 1, (int) ((int32 >> 14) & 0x3F) + 1,
                (int32 >> 6) & 0x1F, int32 & 0x3F, 0);

        return dateTime.toInstant(ZoneOffset.UTC);
    }

    public final Vector3 readVector3() {
        resetBitPos();
        return new Vector3(content().readFloat(), content().readFloat(), content().readFloat());
    }


    public final boolean readBit() {

        ++bitPosition;
        if (bitPosition > 7) {
            bitValue = content().readByte();
            bitPosition = 0;
        }
        return ((bitValue >>> (7 - bitPosition)) & 1) != 0;
    }


    public final int readBit(int bitCount) {
        int value = 0;

        for (var i = bitCount - 1; i >= 0; --i) {
            if (readBit()) {
                value |= (1 << i);
            }
        }
        return value;
    }


    public final void writeBoolean(boolean value) {
        flushBits();
        content().writeBoolean(value);
    }

    public final void writeInt8(int value) {
        flushBits();
        content().writeByte(value);
    }

    public final void writeInt16(int value) {
        flushBits();
        content().writeShort(value);
    }

    public final void writeInt32(int value) {
        flushBits();
        content().writeInt(value);
    }


    public final void writeInt64(long value) {
        flushBits();
        content().writeLong(value);
    }


    public final void writeFloat(float value) {
        flushBits();
        content().writeFloat(value);
    }


    public final void writeDouble(double value) {
        flushBits();
        content().writeDouble(value);
    }


    public final void writeBytes(byte[] src) {
        flushBits();
        content().writeBytes(src);
    }

    public final void writeBytes(byte[] src, int srcIndex, int length) {
        flushBits();
        content().writeBytes(src, srcIndex, length);
    }

    public final void writeBytes(ByteBuffer src) {
        flushBits();
        content().writeBytes(src);
    }

    public final void writeBytes(ByteBuf src) {
        flushBits();
        content().writeBytes(src);
    }

    public final void writeCString(String str) {
        if (StringUtil.isEmpty(str)) {
            writeInt8(0);
            return;
        }
        writeString(str);
        writeInt8(0);
    }

    public final void writeString(String str) {
        if (StringUtil.isEmpty(str)) {
            return;
        }

        byte[] sBytes = str.getBytes(StandardCharsets.UTF_8);
        writeBytes(sBytes);
    }


    public final void writeBytes(byte[] data, int count) {
        flushBits();
        content().writeBytes(data, 0, count);
    }


    public final void writeVector3(Vector3 pos) {
        flushBits();
        writeFloat(pos.x);
        writeFloat(pos.y);
        writeFloat(pos.z);
    }

    public final void writeVector2(Vector2 pos) {
        flushBits();
        writeFloat(pos.x);
        writeFloat(pos.y);
    }

    public final void writePackXYZ(Vector3 pos) {
        int packed = 0;
        packed |= ((int) (pos.x / 0.25f) & 0x7FF);
        packed |= ((int) (pos.y / 0.25f) & 0x7FF) << 11;
        packed |= ((int) (pos.z / 0.25f) & 0x3FF) << 22;
        writeInt32(packed);
    }

    public final boolean writeBit(boolean bit) {
        --bitPosition;
        if (bit) {
            bitValue |= (byte) (1 << bitPosition);
        }
        if (bitPosition == 0) {
            content().writeByte(bitValue);
            bitPosition = 8;
            bitValue = 0;
        }
        return bit;
    }

    public final void writeBits(int value, int bits) {
        for (int i = bits - 1; i >= 0; --i) {
            writeBit(((value >>> i) & 1) != 0);
        }
    }

    public final void writePackedTime(Instant time) {
        LocalDateTime dateTime = LocalDateTime.from(time);
        int packetTime = (dateTime.getYear() - 2000) << 24
                | (dateTime.getMonthValue() - 1) << 20
                | (dateTime.getDayOfMonth() - 1) << 14
                | dateTime.getDayOfWeek().getValue() << 11
                | dateTime.getHour() << 6
                | dateTime.getMinute();
        writeInt32(packetTime);
    }

    public final void writePackedTime() {
        writePackedTime(Instant.now());
    }


    public final void setInt32(int index, int value) {
         content().setInt(index, value);
    }


    public final boolean hasUnfinishedBitPack() {
        return bitPosition != 8;
    }

    public final void flushBits() {
        if (bitPosition == 8) {
            return;
        }

        content().writeByte(bitValue);
        bitValue = 0;
        bitPosition = 8;
    }

    public final void resetBitPos() {
        if (bitPosition > 7) {
            return;
        }

        bitPosition = 8;
        bitValue = 0;
    }


    public final void clear() {
        bitPosition = 8;
        bitValue = 0;
        content().clear();
    }


    public final Position readPosition() {
        resetBitPos();
        return new Position(content().readFloat(), content().readFloat(), content().readFloat());
    }


    public final void writeGuid(ObjectGuid guid) {
        flushBits();
        if (guid == null || guid.isEmpty()) {
            content().writeByte(0);
            content().writeByte(0);
            return;
        }

        long lowValue = guid.lowValue();
        long highValue = guid.highValue();

        int lowLength = 0;
        int lowMask = 0;
        byte[] lowByte = new byte[8];

        for (byte i = 0; lowValue != 0; ++i) {
            if ((lowValue & 0xFF) != 0) {
                lowMask |= (byte) (1 << i);
                lowByte[lowLength++] = (byte) (lowValue & 0xFF);
            }
            lowValue >>>= 8;
        }

        int highLength = 0;
        int highMask = 0;
        byte[] highByte = new byte[8];
        for (byte i = 0; highValue != 0; ++i) {
            if ((highValue & 0xFF) != 0) {
                highMask |= (byte) (1 << i);
                highByte[highLength++] = (byte) (highValue & 0xFF);
            }
            highValue >>>= 8;
        }

        content().writeByte(lowMask);
        content().writeByte(highMask);
        content().writeBytes(lowByte, 0, lowLength);
        content().writeBytes(highByte, 0, highLength);
    }


    public final void writeBytes(WorldPacket packet) {
        flushBits();
        content().writeBytes(packet.content());
    }

    public final void writeXYZ(Position pos) {
        if (pos == null) {
            return;
        }
        flushBits();
        content().writeFloat(pos.getX());
        content().writeFloat(pos.getY());
        content().writeFloat(pos.getZ());
    }

    public final void writeXYZO(Position pos) {
        flushBits();
        content().writeFloat(pos.getX());
        content().writeFloat(pos.getY());
        content().writeFloat(pos.getZ());
        content().writeFloat(pos.getO());
    }

    public boolean release() {
        return content().release();
    }

    public final LocalDateTime getReceivedTime() {
        return m_receivedTime;
    }

    public final void setReceiveTime(LocalDateTime receivedTime) {
        m_receivedTime = receivedTime;
    }


    private long readVarUInt64(byte length) {
        if (length == 0) {
            return 0;
        }
        long guid = 0;
        for (var i = 0; i < 8; i++) {
            if ((1 << i & length) != 0) {
                guid |= (long) content().readByte() << (i * 8);
            }
        }
        return guid;
    }

}
