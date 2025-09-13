package com.github.azeroth.game.networking.packet.bpay;

import com.github.azeroth.game.networking.WorldPacket;

public class BpayShop {

    private int entry;

    private int entryId;

    private int groupID;

    private int productID;

    private int ordering;

    private int vasServiceType;

    private byte storeDeliveryType;
    private BpaydisplayInfo display;


    public final int getEntry() {
        return entry;
    }


    public final void setEntry(int value) {
        entry = value;
    }


    public final int getEntryId() {
        return entryId;
    }


    public final void setEntryId(int value) {
        entryId = value;
    }


    public final int getGroupID() {
        return groupID;
    }


    public final void setGroupID(int value) {
        groupID = value;
    }


    public final int getProductID() {
        return productID;
    }


    public final void setProductID(int value) {
        productID = value;
    }


    public final int getOrdering() {
        return ordering;
    }


    public final void setOrdering(int value) {
        ordering = value;
    }


    public final int getVasServiceType() {
        return vasServiceType;
    }


    public final void setVasServiceType(int value) {
        vasServiceType = value;
    }


    public final byte getStoreDeliveryType() {
        return storeDeliveryType;
    }


    public final void setStoreDeliveryType(byte value) {
        storeDeliveryType = value;
    }

    public final BpayDisplayInfo getDisplay() {
        return display;
    }

    public final void setDisplay(BpayDisplayInfo value) {
        display = value;
    }

    public final void write(WorldPacket this) {
        this.write(getEntryId());
        this.write(getGroupID());
        this.write(getProductID());
        this.write(getOrdering());
        this.write(getVasServiceType());
        this.write(getStoreDeliveryType());
        this.flushBits();
        this.writeBit(getDisplay().has_value());

        if (getDisplay().has_value()) {
            this.flushBits();
            getDisplay().write(this);
        }
    }
}
