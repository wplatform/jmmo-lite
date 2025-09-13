package com.github.azeroth.game.networking.packet.bpay;


import java.util.ArrayList;


public class PurchaseUpdate extends ServerPacket {
    private ArrayList<Bpaypurchase> purchase = new ArrayList<>();

    public PurchaseUpdate() {
        super(ServerOpcode.BattlePayPurchaseUpdate);
    }

    public final ArrayList<BpayPurchase> getPurchase() {
        return purchase;
    }

    public final void setPurchase(ArrayList<BpayPurchase> value) {
        purchase = value;
    }

    @Override
    public void write() {
        this.writeInt32((int) getPurchase().size());

        for (var purchaseData : getPurchase()) {
            purchaseData.write(this);
        }
    }
}
