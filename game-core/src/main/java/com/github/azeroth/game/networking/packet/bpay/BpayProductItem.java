package com.github.azeroth.game.networking.packet.bpay;

import com.github.azeroth.game.networking.WorldPacket;

public class BpayProductItem {
    private int entry;
    private int ID;
    private byte unkByte;
    private int itemID;
    private int quantity;
    private int unkInt1;
    private int unkInt2;
    private boolean isPet;
    private int petResult;
    private BpaydisplayInfo display;

    public final int getEntry() {
        return entry;
    }

    public final void setEntry(int value) {
        entry = value;
    }

    public final int getID() {
        return ID;
    }

    public final void setID(int value) {
        ID = value;
    }

    public final byte getUnkByte() {
        return unkByte;
    }

    public final void setUnkByte(byte value) {
        unkByte = value;
    }

    public final int getItemID() {
        return itemID;
    }

    public final void setItemID(int value) {
        itemID = value;
    }

    public final int getQuantity() {
        return quantity;
    }

    public final void setQuantity(int value) {
        quantity = value;
    }

    public final int getUnkInt1() {
        return unkInt1;
    }

    public final void setUnkInt1(int value) {
        unkInt1 = value;
    }

    public final int getUnkInt2() {
        return unkInt2;
    }

    public final void setUnkInt2(int value) {
        unkInt2 = value;
    }

    public final boolean isPet() {
        return isPet;
    }

    public final void setPet(boolean value) {
        isPet = value;
    }

    public final int getPetResult() {
        return petResult;
    }

    public final void setPetResult(int value) {
        petResult = value;
    }

    public final BpayDisplayInfo getDisplay() {
        return display;
    }

    public final void setDisplay(BpayDisplayInfo value) {
        display = value;
    }

    public final void write(WorldPacket this) {
        this.write(getID());
        this.write(getUnkByte());
        this.write(getItemID());
        this.write(getQuantity());
        this.write(getUnkInt1());
        this.write(getUnkInt2());
        this.writeBit(isPet());
        this.writeBit(getPetResult().has_value());
        this.writeBit(getDisplay().has_value());

        if (getPetResult().has_value()) {
            this.writeBits(getPetResult(), 4);
        }

        this.flushBits();

        if (getDisplay().has_value()) {
            getDisplay().write(this);
        }
    }
}
