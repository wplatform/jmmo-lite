package com.github.azeroth.game.map;


public final class UpdateBossStateSaveDataEvent {
    public DungeonEncounter dungeonEncounter;
    public int bossId;
    public EncounterState newState = EncounterState.values()[0];

    public UpdateBossStateSaveDataEvent() {
    }

    public UpdateBossStateSaveDataEvent(DungeonEncounter dungeonEncounter, int bossId, EncounterState state) {
        dungeonEncounter = dungeonEncounter;
        bossId = bossId;
        newState = state;
    }

    public UpdateBossStateSaveDataEvent clone() {
        UpdateBossStateSaveDataEvent varCopy = new UpdateBossStateSaveDataEvent();

        varCopy.dungeonEncounter = this.dungeonEncounter;
        varCopy.bossId = this.bossId;
        varCopy.newState = this.newState;

        return varCopy;
    }
}
