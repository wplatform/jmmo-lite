package com.github.azeroth.game.networking.packet.bpay;


import com.github.azeroth.game.networking.packet.itemInstance;

import java.util.ArrayList;


public class DeliveryEnded extends ServerPacket {
    private ArrayList<itemInstance> item = new ArrayList<>();
    private long distributionID = 0;

    public DeliveryEnded() {
        super(ServerOpcode.BattlePayDeliveryEnded);
    }

    public final ArrayList<itemInstance> getItem() {
        return item;
    }

    public final void setItem(ArrayList<itemInstance> value) {
        item = value;
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

        this.write(getItem().size());

        for (var itemData : getItem()) {
            itemData.write(this);
        }
    }
}
