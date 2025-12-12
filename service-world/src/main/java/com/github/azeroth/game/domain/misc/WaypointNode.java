package com.github.azeroth.game.domain.misc;


public class WaypointNode {
    public int id;
    public float x, y, z;
    public Float orientation = null;
    public int delay;
    public int eventId;
    public WaypointMoveType moveType;
    public byte eventChance;

    public WaypointNode() {
        moveType = WaypointMoveType.RUN;
    }


    public WaypointNode(int id, float _x, float _y, float _z, Float orientation) {
        this(id, _x, _y, _z, orientation, 0);
    }

    public WaypointNode(int id, float _x, float _y, float _z) {
        this(id, _x, _y, _z, null, 0);
    }

    public WaypointNode(int id, float _x, float _y, float _z, Float orientation, int delay) {
        this.id = id;
        this.x = _x;
        this.y = _y;
        this.z = _z;
        this.orientation = orientation;
        this.delay = delay;
        this.eventId = 0;
        this.moveType = WaypointMoveType.WALK;
        this.eventChance = 100;
    }
}
