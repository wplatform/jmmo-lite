package com.github.azeroth.game.map;

public class InstanceLockData {
    private String data;
    private int completedEncountersMask;
    private int entranceWorldSafeLocId;

    public final String getData() {
        return data;
    }

    public final void setData(String value) {
        data = value;
    }

    public final int getCompletedEncountersMask() {
        return completedEncountersMask;
    }

    public final void setCompletedEncountersMask(int value) {
        completedEncountersMask = value;
    }

    public final int getEntranceWorldSafeLocId() {
        return entranceWorldSafeLocId;
    }

    public final void setEntranceWorldSafeLocId(int value) {
        entranceWorldSafeLocId = value;
    }
}
