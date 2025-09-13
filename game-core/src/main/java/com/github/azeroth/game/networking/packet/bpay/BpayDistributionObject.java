package com.github.azeroth.game.networking.packet.bpay;

import com.github.azeroth.game.networking.WorldPacket;

public class BpayDistributionObject {
    private Bpayproduct product;
    private ObjectGuid targetPlayer = ObjectGuid.EMPTY;

    private long distributionID = 0;

    private long purchaseID = 0;

    private int status = 0;

    private int productID = 0;

    private int targetVirtualRealm = 0;

    private int targetNativeRealm = 0;
    private boolean revoked = false;

    public final BpayProduct getProduct() {
        return product;
    }

    public final void setProduct(BpayProduct value) {
        product = value;
    }

    public final ObjectGuid getTargetPlayer() {
        return targetPlayer;
    }

    public final void setTargetPlayer(ObjectGuid value) {
        targetPlayer = value;
    }


    public final long getDistributionID() {
        return distributionID;
    }


    public final void setDistributionID(long value) {
        distributionID = value;
    }


    public final long getPurchaseID() {
        return purchaseID;
    }


    public final void setPurchaseID(long value) {
        purchaseID = value;
    }


    public final int getStatus() {
        return status;
    }


    public final void setStatus(int value) {
        status = value;
    }


    public final int getProductID() {
        return productID;
    }


    public final void setProductID(int value) {
        productID = value;
    }


    public final int getTargetVirtualRealm() {
        return targetVirtualRealm;
    }


    public final void setTargetVirtualRealm(int value) {
        targetVirtualRealm = value;
    }


    public final int getTargetNativeRealm() {
        return targetNativeRealm;
    }


    public final void setTargetNativeRealm(int value) {
        targetNativeRealm = value;
    }

    public final boolean getRevoked() {
        return revoked;
    }

    public final void setRevoked(boolean value) {
        revoked = value;
    }

    public final void write(WorldPacket this) {
        this.write(getDistributionID());

        this.write(getStatus());
        this.write(getProductID());

        this.write(getTargetPlayer());
        this.write(getTargetVirtualRealm());
        this.write(getTargetNativeRealm());

        this.write(getPurchaseID());
        this.writeBit(getProduct().has_value());
        this.writeBit(getRevoked());
        this.flushBits();

        if (getProduct().has_value()) {
            getProduct().write(this);
        }
    }
}
