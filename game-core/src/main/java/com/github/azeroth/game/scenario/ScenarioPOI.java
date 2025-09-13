package com.github.azeroth.game.scenario;

import java.util.ArrayList;


public class ScenarioPOI {
    public int blobIndex;
    public int mapID;
    public int uiMapID;
    public int priority;
    public int flags;
    public int worldEffectID;
    public int playerConditionID;
    public int navigationPlayerConditionID;
    public ArrayList<ScenarioPOIPoint> points = new ArrayList<>();

    public ScenarioPOI(int blobIndex, int mapID, int uiMapID, int priority, int flags, int worldEffectID, int playerConditionID, int navigationPlayerConditionID, ArrayList<ScenarioPOIPoint> points) {
        blobIndex = blobIndex;
        mapID = mapID;
        uiMapID = uiMapID;
        priority = priority;
        flags = flags;
        worldEffectID = worldEffectID;
        playerConditionID = playerConditionID;
        navigationPlayerConditionID = navigationPlayerConditionID;
        points = points;
    }
}
