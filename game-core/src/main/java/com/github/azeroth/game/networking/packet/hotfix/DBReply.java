package com.github.azeroth.game.networking.packet.hotfix;


public class DBReply extends ServerPacket {
    public int tableHash;
    public int timestamp;
    public int recordID;
    public HotfixRecord.status status = HotfixRecord.status.Invalid;

    public byteBuffer data = new byteBuffer();

    public DBReply() {
        super(ServerOpcode.DbReply);
    }

    @Override
    public void write() {
        this.writeInt32(tableHash);
        this.writeInt32(recordID);
        this.writeInt32(timestamp);
        this.writeBits((byte) status.getValue(), 3);
        this.writeInt32(data.getSize());
        this.writeBytes(data.getData());
    }
}
