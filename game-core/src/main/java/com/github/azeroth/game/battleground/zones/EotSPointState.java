package com.github.azeroth.game.battleground.zones;


import java.util.HashMap;

enum EotSPointState {
    NoOwner(0),
    Uncontrolled(0),
    UnderControl(3);

    public static final int SIZE = Integer.SIZE;
    private static HashMap<Integer, EotSPointState> mappings;
    private int intValue;

    private EotSPointState(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static HashMap<Integer, EotSPointState> getMappings() {
        if (mappings == null) {
            synchronized (EotSPointState.class) {
                if (mappings == null) {
                    mappings = new HashMap<Integer, EotSPointState>();
                }
            }
        }
        return mappings;
    }

    public static EotSPointState forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
