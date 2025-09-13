package com.github.azeroth.game.networking.packet.bpay;

import com.github.azeroth.game.networking.WorldPacket;

public class BpayPurchase {

    private long purchaseID = 0;

    private long unkLong = 0;

    private long unkLong2 = 0;

    private int status = 0;

    private int resultCode = 0;

    private int productID = 0;

    private int unkInt = 0;
    private String walletName = "";


    public final long getPurchaseID() {
        return purchaseID;
    }


    public final void setPurchaseID(long value) {
        purchaseID = value;
    }


    public final long getUnkLong() {
        return unkLong;
    }


    public final void setUnkLong(long value) {
        unkLong = value;
    }


    public final long getUnkLong2() {
        return unkLong2;
    }


    public final void setUnkLong2(long value) {
        unkLong2 = value;
    }


    public final int getStatus() {
        return status;
    }


    public final void setStatus(int value) {
        status = value;
    }


    public final int getResultCode() {
        return resultCode;
    }


    public final void setResultCode(int value) {
        resultCode = value;
    }


    public final int getProductID() {
        return productID;
    }


    public final void setProductID(int value) {
        productID = value;
    }


    public final int getUnkInt() {
        return unkInt;
    }


    public final void setUnkInt(int value) {
        unkInt = value;
    }

    public final String getWalletName() {
        return walletName;
    }

    public final void setWalletName(String value) {
        walletName = value;
    }

    public final void write(WorldPacket this) {
        this.write(getPurchaseID());
        this.write(getUnkLong());
        this.write(getUnkLong2());
        this.write(getStatus());
        this.write(getResultCode());
        this.write(getProductID());
        this.write(getUnkInt());
        this.writeBits(getWalletName().length(), 8);
        this.writeString(getWalletName());
    }
}
