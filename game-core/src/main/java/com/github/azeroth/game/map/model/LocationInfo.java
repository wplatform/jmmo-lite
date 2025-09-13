package com.github.azeroth.game.map.model;

public class LocationInfo {
    public int rootId;
    public ModelInstance hitInstance;
    public GroupModel hitModel;
    public GameObjectModel dynHitModel;
    public float groundZ;
    public boolean result;

    public LocationInfo() {
        groundZ = Float.NEGATIVE_INFINITY;
    }
}
