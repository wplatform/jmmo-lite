package com.github.azeroth.game.networking.packet.movement;


public class TransferAborted extends ServerPacket {
    public int mapID;
    public byte arg;
    public int mapDifficultyXConditionID;
    public TransferAbortReason transfertAbort = TransferAbortReason.values()[0];

    public TransferAborted() {
        super(ServerOpcode.TransferAborted);
    }

    @Override
    public void write() {
        this.writeInt32(mapID);
        this.writeInt8(arg);
        this.writeInt32(mapDifficultyXConditionID);
        this.writeBits(transfertAbort, 6);
        this.flushBits();
    }
}
