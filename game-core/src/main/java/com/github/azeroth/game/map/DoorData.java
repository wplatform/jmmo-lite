package com.github.azeroth.game.map;


public class DoorData {
    private int entry;
    private int bossId;
    private Doortype type = DoorType.values()[0];

    public DoorData(int entry, int bossid, DoorType doorType) {
        setEntry(entry);
        setBossId(bossid);
        setType(doorType);
    }

    public final int getEntry() {
        return entry;
    }

    public final void setEntry(int value) {
        entry = value;
    }

    public final int getBossId() {
        return bossId;
    }

    public final void setBossId(int value) {
        bossId = value;
    }

    public final DoorType getType() {
        return type;
    }

    public final void setType(DoorType value) {
        type = value;
    }
}
