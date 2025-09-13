package com.github.azeroth.game.networking.packet.bpay;


public class ConfirmPurchase extends ServerPacket {

    private long purchaseID = 0;

    private int serverToken = 0;

    public confirmPurchase() {
        super(ServerOpcode.BattlePayConfirmPurchase);
    }


    public final long getPurchaseID() {
        return purchaseID;
    }


    public final void setPurchaseID(long value) {
        purchaseID = value;
    }


    public final int getServerToken() {
        return serverToken;
    }


    public final void setServerToken(int value) {
        serverToken = value;
    }

    @Override
    public void write() {
        this.write(getPurchaseID());
        this.write(getServerToken());
    }
}
