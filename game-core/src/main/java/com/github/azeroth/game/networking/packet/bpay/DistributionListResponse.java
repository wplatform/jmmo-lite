package com.github.azeroth.game.networking.packet.bpay;


import java.util.ArrayList;


public class DistributionListResponse extends ServerPacket {
    private int result = 0;
    private ArrayList<BpaydistributionObject> distributionObject = new ArrayList<>();

    public DistributionListResponse() {
        super(ServerOpcode.BattlePayGetDistributionListResponse);
    }

    public final int getResult() {
        return result;
    }

    public final void setResult(int value) {
        result = value;
    }

    public final ArrayList<BpayDistributionObject> getDistributionObject() {
        return distributionObject;
    }

    public final void setDistributionObject(ArrayList<BpayDistributionObject> value) {
        distributionObject = value;
    }

    @Override
    public void write() {
        this.write(getResult());
        this.writeBits((int) getDistributionObject().size(), 11);

        for (var objectData : getDistributionObject()) {
            objectData.write(this);
        }
    }
}
