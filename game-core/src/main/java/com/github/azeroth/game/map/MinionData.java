package com.github.azeroth.game.map;

public class MinionData {
    private int entry;
    private int bossId;

    public MinionData(int entry, int _bossid) {
        setEntry(entry);
        setBossId(_bossid);
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
}
