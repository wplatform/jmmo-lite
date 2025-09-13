package com.github.azeroth.game.group;

public class RaidMarker {
    public Worldlocation location;
    public ObjectGuid transportGUID = ObjectGuid.EMPTY;


    public RaidMarker(int mapId, float positionX, float positionY, float positionZ) {
        this(mapId, positionX, positionY, positionZ, null);
    }

    public RaidMarker(int mapId, float positionX, float positionY, float positionZ, ObjectGuid transportGuid) {
        location = new worldLocation(mapId, positionX, positionY, positionZ);
        transportGUID = transportGuid;
    }
}
