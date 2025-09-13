package com.github.azeroth.game.combat;

public enum OnlineState {
    online(2),
    Suppressed(1),
    Offline(0);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, OnlineState> mappings;
    private int intValue;

    private OnlineState(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, OnlineState> getMappings() {
        if (mappings == null) {
            synchronized (OnlineState.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, OnlineState>();
                }
            }
        }
        return mappings;
    }

    public static OnlineState forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
