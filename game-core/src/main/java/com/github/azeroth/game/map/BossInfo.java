package com.github.azeroth.game.map;


import com.github.azeroth.game.domain.map.MapDefine;

import java.util.ArrayList;


public class BossInfo {
    private Encounterstate state = EncounterState.values()[0];
    private ArrayList<ObjectGuid>[] door = new ArrayList<ObjectGuid>[DoorType.max.getValue()];
    private ArrayList<ObjectGuid> minion = new ArrayList<>();
    private ArrayList<Areaboundary> boundary = new ArrayList<>();
    private DungeonEncounter[] dungeonEncounters = new DungeonEncounter[MapDefine.MaxDungeonEncountersPerBoss];

    public bossInfo() {
        setState(EncounterState.ToBeDecided);

        for (var i = 0; i < DoorType.max.getValue(); ++i) {
            getDoor()[i] = new ArrayList<>();
        }
    }

    public final EncounterState getState() {
        return state;
    }

    public final void setState(EncounterState value) {
        state = value;
    }

    public final ArrayList<ObjectGuid>[] getDoor() {
        return door;
    }

    public final void setDoor(ArrayList<ObjectGuid>[] value) {
        door = value;
    }

    public final ArrayList<ObjectGuid> getMinion() {
        return minion;
    }

    public final void setMinion(ArrayList<ObjectGuid> value) {
        minion = value;
    }

    public final ArrayList<AreaBoundary> getBoundary() {
        return boundary;
    }

    public final void setBoundary(ArrayList<AreaBoundary> value) {
        boundary = value;
    }

    public final DungeonEncounter[] getDungeonEncounters() {
        return dungeonEncounters;
    }

    public final void setDungeonEncounters(DungeonEncounter[] value) {
        dungeonEncounters = value;
    }

    public final DungeonEncounter getDungeonEncounterForDifficulty(Difficulty difficulty) {
        return getDungeonEncounters().FirstOrDefault(dungeonEncounter ->
        {
            if (dungeonEncounter != null) {
                dungeonEncounter.difficultyID;
            }
        } == 0 || Difficulty.forValue(dungeonEncounter == null ? null : dungeonEncounter.difficultyID) == difficulty);
    }
}
