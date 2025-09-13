package com.github.azeroth.game.networking.packet.bpay;


public class DistributionUpdate extends ServerPacket {
    private BpaydistributionObject distributionObject = new bpayDistributionObject();

    public DistributionUpdate() {
        super(ServerOpcode.BattlePayDistributionUpdate);
    }

    public final BpayDistributionObject getDistributionObject() {
        return distributionObject;
    }

    public final void setDistributionObject(BpayDistributionObject value) {
        distributionObject = value;
    }

    @Override
    public void write() {
        getDistributionObject().write(this);
    }
}
