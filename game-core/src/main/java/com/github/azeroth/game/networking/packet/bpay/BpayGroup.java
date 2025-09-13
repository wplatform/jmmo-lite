package com.github.azeroth.game.networking.packet.bpay;

import com.github.azeroth.game.networking.WorldPacket;

public class BpayGroup {
    private int entry;
    private int groupId;
    private int iconFileDataID;
    private byte displayType;
    private int ordering;
    private int unk;
    private String name = "";
    private String description = "";

    public final int getEntry() {
        return entry;
    }

    public final void setEntry(int value) {
        entry = value;
    }

    public final int getGroupId() {
        return groupId;
    }

    public final void setGroupId(int value) {
        groupId = value;
    }

    public final int getIconFileDataID() {
        return iconFileDataID;
    }

    public final void setIconFileDataID(int value) {
        iconFileDataID = value;
    }

    public final byte getDisplayType() {
        return displayType;
    }

    public final void setDisplayType(byte value) {
        displayType = value;
    }

    public final int getOrdering() {
        return ordering;
    }

    public final void setOrdering(int value) {
        ordering = value;
    }

    public final int getUnk() {
        return unk;
    }

    public final void setUnk(int value) {
        unk = value;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String value) {
        name = value;
    }

    public final String getDescription() {
        return description;
    }

    public final void setDescription(String value) {
        description = value;
    }

    public final void write(WorldPacket this) {
        this.write(getGroupId());
        this.write(getIconFileDataID());
        this.write(getDisplayType());
        this.write(getOrdering());
        this.write(getUnk());
        this.writeBits(getName().length(), 8);
        this.writeBits(getDescription().length() + 1, 24);
        this.flushBits();
        this.writeString(getName());

        if (!StringUtil.isEmpty(getDescription())) {
            this.write(getDescription());
        }
    }
}
