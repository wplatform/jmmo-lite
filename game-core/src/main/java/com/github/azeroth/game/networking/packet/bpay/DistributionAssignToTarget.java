package com.github.azeroth.game.networking.packet.bpay;

import com.github.azeroth.game.networking.WorldPacket;

public class DistributionAssignToTarget extends ClientPacket {
    private ObjectGuid targetCharacter = ObjectGuid.EMPTY;
    private long distributionID = 0;
    private int productID = 0;
    private short specializationID = 0;
    private short choiceID = 0;

    public DistributionAssignToTarget(WorldPacket packet) {
        super(packet);
    }

    public final ObjectGuid getTargetCharacter() {
        return targetCharacter;
    }

    public final void setTargetCharacter(ObjectGuid value) {
        targetCharacter = value;
    }

    public final long getDistributionID() {
        return distributionID;
    }

    public final void setDistributionID(long value) {
        distributionID = value;
    }

    public final int getProductID() {
        return productID;
    }

    public final void setProductID(int value) {
        productID = value;
    }

    public final short getSpecializationID() {
        return specializationID;
    }

    public final void setSpecializationID(short value) {
        specializationID = value;
    }

    public final short getChoiceID() {
        return choiceID;
    }

    public final void setChoiceID(short value) {
        choiceID = value;
    }

    @Override
    public void read() {
        setProductID(this.readUInt());
        setDistributionID(this.readUInt64());
        setTargetCharacter(this.readPackedGuid());
        setSpecializationID(this.readUInt16());
        setChoiceID(this.readUInt16());
    }
}
