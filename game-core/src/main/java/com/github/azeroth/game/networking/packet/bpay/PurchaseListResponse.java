package com.github.azeroth.game.networking.packet.bpay;


import java.util.ArrayList;


public class PurchaseListResponse extends ServerPacket {
    private int result = 0;
    private ArrayList<Bpaypurchase> purchase = new ArrayList<>();

    public PurchaseListResponse() {
        super(ServerOpcode.BattlePayGetPurchaseListResponse);
    }

    public final int getResult() {
        return result;
    }

    public final void setResult(int value) {
        result = value;
    }

    public final ArrayList<BpayPurchase> getPurchase() {
        return purchase;
    }

    public final void setPurchase(ArrayList<BpayPurchase> value) {
        purchase = value;
    }

    @Override
    public void write() {
        this.write(getResult());
        this.writeInt32((int) getPurchase().size());

        for (var purchaseData : getPurchase()) {
            purchaseData.write(this);
        }
    }
}
