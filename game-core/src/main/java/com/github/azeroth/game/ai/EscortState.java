package com.github.azeroth.game.ai;

public enum EscortState {
    NONE(0x00), //nothing in progress
    Escorting(0x01), //escort are in progress
    Returning(0x02), //escort is returning after being in combat
    paused(0x04); //will not proceed with waypoints before state is removed

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, EscortState> mappings;
    private int intValue;

    private EscortState(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, EscortState> getMappings() {
        if (mappings == null) {
            synchronized (EscortState.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, EscortState>();
                }
            }
        }
        return mappings;
    }

    public static EscortState forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
