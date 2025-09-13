package com.github.azeroth.game.networking.packet.achievement;


public class CriteriaUpdate extends ServerPacket {
    public int criteriaID;
    public long quantity;
    public ObjectGuid playerGUID = ObjectGuid.EMPTY;
    public int flags;
    public long currentTime;
    public long elapsedTime;
    public long creationTime;
    public Long rafAcceptanceID = null;

    public CriteriaUpdate() {
        super(ServerOpcode.CriteriaUpdate);
    }

    @Override
    public void write() {
        this.writeInt32(criteriaID);
        this.writeInt64(quantity);
        this.writeGuid(playerGUID);
        this.writeInt32(flags);
        this.writePackedTime(currentTime);
        this.writeInt64(elapsedTime);
        this.writeInt64(creationTime);
        this.writeBit(rafAcceptanceID != null);
        this.flushBits();

        if (rafAcceptanceID != null) {
            this.writeInt64(rafAcceptanceID.longValue());
        }
    }
}
