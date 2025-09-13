package com.github.azeroth.game;


import java.util.HashMap;

public enum GameEventState {
    NORMAL(0), // standard game events
    WorldInactive(1), // not yet started
    WorldConditions(2), // condition matching phase
    WorldNextPhase(3), // conditions are met, now 'length' timer to start next event
    WorldFinished(4), // next events are started, unapply this one
    internal(5); // never handled in update

    public static final int SIZE = Integer.SIZE;
    private static HashMap<Integer, GameEventState> mappings;
    private int intValue;

    private GameEventState(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static HashMap<Integer, GameEventState> getMappings() {
        if (mappings == null) {
            synchronized (GameEventState.class) {
                if (mappings == null) {
                    mappings = new HashMap<Integer, GameEventState>();
                }
            }
        }
        return mappings;
    }

    public static GameEventState forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
