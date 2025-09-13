package com.github.azeroth.game.networking.packet.clientconfig;


public class UpdateAccountData extends ServerPacket {
    public ObjectGuid player = ObjectGuid.EMPTY;
    public long time; // UnixTime
    public int size; // decompressed size
    public AccountdataTypes dataType = AccountDataTypes.forValue(0);
    public ByteBuffer compressedData;

    public UpdateAccountData() {
        super(ServerOpcode.UpdateAccountData);
    }

    @Override
    public void write() {
        this.writeGuid(player);
        this.writeInt64(time);
        this.writeInt32(size);
        this.writeBits(dataType, 4);

        if (compressedData == null) {
            this.writeInt32(0);
        } else {
            var bytes = compressedData.getData();
            this.writeInt32(bytes.length);
            this.writeBytes(bytes);
        }
    }
}
