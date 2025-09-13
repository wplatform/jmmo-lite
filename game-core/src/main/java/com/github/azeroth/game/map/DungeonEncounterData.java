package com.github.azeroth.game.map;

public class DungeonEncounterData {
    private int bossId;
    private int[] dungeonEncounterId = new int[4];

    public DungeonEncounterData(int bossId, Integer... dungeonEncounterIds) {
        setBossId(bossId);
        setDungeonEncounterId(dungeonEncounterIds);
    }

    public final int getBossId() {
        return bossId;
    }

    public final void setBossId(int value) {
        bossId = value;
    }

    public final int[] getDungeonEncounterId() {
        return dungeonEncounterId;
    }

    public final void setDungeonEncounterId(int[] value) {
        dungeonEncounterId = value;
    }
}
