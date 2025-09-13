package com.github.azeroth.game.map;

public class BossBoundaryEntry {
    private int bossId;
    private Areaboundary boundary;

    public BossBoundaryEntry(int bossId, AreaBoundary boundary) {
        setBossId(bossId);
        setBoundary(boundary);
    }

    public final int getBossId() {
        return bossId;
    }

    public final void setBossId(int value) {
        bossId = value;
    }

    public final AreaBoundary getBoundary() {
        return boundary;
    }

    public final void setBoundary(AreaBoundary value) {
        boundary = value;
    }
}
