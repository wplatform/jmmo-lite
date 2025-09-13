package com.github.azeroth.game.battleground.zones;


enum WSGFlagState {
    OnBase(1),
    OnPlayer(2),
    OnGround(3),
    WaitRespawn(4);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, WSGFlagState> mappings;
    private int intValue;

    private WSGFlagState(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, WSGFlagState> getMappings() {
        if (mappings == null) {
            synchronized (WSGFlagState.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, WSGFlagState>();
                }
            }
        }
        return mappings;
    }

    public static WSGFlagState forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
