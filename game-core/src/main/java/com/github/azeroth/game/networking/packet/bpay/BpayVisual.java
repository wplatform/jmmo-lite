package com.github.azeroth.game.networking.packet.bpay;

import com.github.azeroth.game.networking.WorldPacket;

public class BpayVisual {

    private int entry;
    private String name = "";

    private int displayId;

    private int visualId;

    private int unk;

    private int displayInfoEntry;


    public final int getEntry() {
        return entry;
    }


    public final void setEntry(int value) {
        entry = value;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String value) {
        name = value;
    }


    public final int getDisplayId() {
        return displayId;
    }


    public final void setDisplayId(int value) {
        displayId = value;
    }


    public final int getVisualId() {
        return visualId;
    }


    public final void setVisualId(int value) {
        visualId = value;
    }


    public final int getUnk() {
        return unk;
    }


    public final void setUnk(int value) {
        unk = value;
    }


    public final int getDisplayInfoEntry() {
        return displayInfoEntry;
    }


    public final void setDisplayInfoEntry(int value) {
        displayInfoEntry = value;
    }

    public final void write(WorldPacket this) {
        this.writeBits(getName().length(), 10);
        this.flushBits();
        this.write(getDisplayId());
        this.write(getVisualId());
        this.write(getUnk());
        this.writeString(getName());
    }
}
