package com.github.azeroth.game.networking.packet.bpay;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class BpayProduct {
    private int entry;
    private int productId;
    private byte type;
    private int flags;
    private int unk1;
    private int displayId;
    private int itemId;
    private int unk4;
    private int unk5;
    private int unk6;
    private int unk7;
    private int unk8;
    private int unk9;
    private String unkString = "";
    private boolean unkBit;
    private int unkBits;
    private ArrayList<BpayProductItem> items = new ArrayList<>();
    private BpaydisplayInfo display;
    private String name;

    public final int getEntry() {
        return entry;
    }

    public final void setEntry(int value) {
        entry = value;
    }

    public final int getProductId() {
        return productId;
    }

    public final void setProductId(int value) {
        productId = value;
    }

    public final byte getType() {
        return type;
    }

    public final void setType(byte value) {
        type = value;
    }

    public final int getFlags() {
        return flags;
    }

    public final void setFlags(int value) {
        flags = value;
    }

    public final int getUnk1() {
        return unk1;
    }

    public final void setUnk1(int value) {
        unk1 = value;
    }

    public final int getDisplayId() {
        return displayId;
    }

    public final void setDisplayId(int value) {
        displayId = value;
    }

    public final int getItemId() {
        return itemId;
    }

    public final void setItemId(int value) {
        itemId = value;
    }

    public final int getUnk4() {
        return unk4;
    }

    public final void setUnk4(int value) {
        unk4 = value;
    }

    public final int getUnk5() {
        return unk5;
    }

    public final void setUnk5(int value) {
        unk5 = value;
    }

    public final int getUnk6() {
        return unk6;
    }

    public final void setUnk6(int value) {
        unk6 = value;
    }

    public final int getUnk7() {
        return unk7;
    }

    public final void setUnk7(int value) {
        unk7 = value;
    }

    public final int getUnk8() {
        return unk8;
    }

    public final void setUnk8(int value) {
        unk8 = value;
    }

    public final int getUnk9() {
        return unk9;
    }

    public final void setUnk9(int value) {
        unk9 = value;
    }

    public final String getUnkString() {
        return unkString;
    }

    public final void setUnkString(String value) {
        unkString = value;
    }

    public final boolean getUnkBit() {
        return unkBit;
    }

    public final void setUnkBit(boolean value) {
        unkBit = value;
    }

    public final int getUnkBits() {
        return unkBits;
    }

    public final void setUnkBits(int value) {
        unkBits = value;
    }

    public final ArrayList<BpayProductItem> getItems() {
        return items;
    }

    public final void setItems(ArrayList<BpayProductItem> value) {
        items = value;
    }

    public final BpayDisplayInfo getDisplay() {
        return display;
    }

    public final void setDisplay(BpayDisplayInfo value) {
        display = value;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String value) {
        name = value;
    }

    public final void write(WorldPacket this) {
        this.write(getProductId());
        this.write(getType());
        this.write(getFlags());
        this.write(getUnk1());
        this.write(getDisplayId());
        this.write(getItemId());
        this.write(getUnk4());
        this.write(getUnk5());
        this.write(getUnk6());
        this.write(getUnk7());
        this.write(getUnk8());
        this.write(getUnk9());
        this.writeBits(getUnkString().length(), 8);
        this.writeBit(getUnkBit());
        this.writeBit(getUnkBits().has_value());
        this.writeBits(getItems().size(), 7);
        this.writeBit(getDisplay().has_value());

        if (getUnkBits().has_value()) {
            this.writeBits(getUnkBits(), 4);
        }

        this.flushBits();

        for (var item : getItems()) {
            item.write(this);
        }

        this.writeString(getUnkString());

        if (getDisplay().has_value()) {
            getDisplay().write(this);
        }
    }
}
