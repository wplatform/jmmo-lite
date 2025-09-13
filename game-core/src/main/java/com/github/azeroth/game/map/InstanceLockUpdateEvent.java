package com.github.azeroth.game.map;

public final class InstanceLockUpdateEvent {

    public int instanceId;
    public String newData;

    public int instanceCompletedEncountersMask;
    public DungeonEncounterRecord completedEncounter;

    public Integer entranceWorldSafeLocId = null;

    public InstanceLockUpdateEvent() {
    }


    public InstanceLockUpdateEvent(int instanceId, String newData, int instanceCompletedEncountersMask, DungeonEncounterRecord completedEncounter, Integer entranceWorldSafeLocId) {
        instanceId = instanceId;
        newData = newData;
        instanceCompletedEncountersMask = instanceCompletedEncountersMask;
        completedEncounter = completedEncounter;
        entranceWorldSafeLocId = entranceWorldSafeLocId;
    }

    public InstanceLockUpdateEvent clone() {
        InstanceLockUpdateEvent varCopy = new InstanceLockUpdateEvent();

        varCopy.instanceId = this.instanceId;
        varCopy.newData = this.newData;
        varCopy.instanceCompletedEncountersMask = this.instanceCompletedEncountersMask;
        varCopy.completedEncounter = this.completedEncounter;
        varCopy.entranceWorldSafeLocId = this.entranceWorldSafeLocId;

        return varCopy;
    }
}
