package com.github.azeroth.game.networking.packet.clientconfig;


import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class UserClientUpdateAccountData extends ClientPacket {
    public ObjectGuid playerGuid = ObjectGuid.EMPTY;
    public long time; // UnixTime
    public int size; // decompressed size
    public AccountdataTypes dataType = AccountDataTypes.forValue(0);
    public ByteBuffer compressedData;

    public UserClientUpdateAccountData(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        playerGuid = this.readPackedGuid();
        time = this.readInt64();
        size = this.readUInt32();
        dataType = AccountDataTypes.forValue(this.<Integer>readBit(4));

        var compressedSize = this.readUInt32();

        if (compressedSize != 0) {
            compressedData = new byteBuffer(this.readBytes(compressedSize));
        }
    }
}
