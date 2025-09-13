package com.github.azeroth.game.networking.packet.bpay;

import com.github.azeroth.game.networking.WorldPacket;

public class ConfirmPurchaseResponse extends ClientPacket {
    private long clientCurrentPriceFixedPoint = 0;
    private int serverToken = 0;
    private boolean confirmPurchase = false;

    public ConfirmPurchaseResponse(WorldPacket packet) {
        super(packet);
    }

    public final long getClientCurrentPriceFixedPoint() {
        return clientCurrentPriceFixedPoint;
    }

    public final void setClientCurrentPriceFixedPoint(long value) {
        clientCurrentPriceFixedPoint = value;
    }

    public final int getServerToken() {
        return serverToken;
    }

    public final void setServerToken(int value) {
        serverToken = value;
    }

    public final boolean getConfirmPurchase() {
        return confirmPurchase;
    }

    public final void setConfirmPurchase(boolean value) {
        confirmPurchase = value;
    }

    @Override
    public void read() {
        setConfirmPurchase(this.ReadBool());
        setServerToken(this.readUInt());
        setClientCurrentPriceFixedPoint(this.readUInt64());
    }
}
