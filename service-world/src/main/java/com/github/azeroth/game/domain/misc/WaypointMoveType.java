package com.github.azeroth.game.domain.misc;


public enum WaypointMoveType {
    WALK,
    RUN,
    LAND,
    TAKEOFF,
    MAX;

    public static WaypointMoveType forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
