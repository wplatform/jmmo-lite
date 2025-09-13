package com.github.azeroth.game.networking.packet.bpay;


public class BattlePayDeliveryStarted extends ServerPacket {

    private long distributionID = 0;

    public BattlePayDeliveryStarted() {
        super(ServerOpcode.BattlePayDeliveryStarted);
    }


    public final long getDistributionID() {
        return distributionID;
    }


    public final void setDistributionID(long value) {
        distributionID = value;
    }

    @Override
    public void write() {
        this.write(getDistributionID());
    }
}
