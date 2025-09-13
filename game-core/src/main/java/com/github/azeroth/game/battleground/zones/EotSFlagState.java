package com.github.azeroth.game.battleground.zones;


import java.util.HashMap;

enum EotSFlagState {
    OnBase(0),
    WaitRespawn(1),
    OnPlayer(2),
    OnGround(3);

    public static final int SIZE = Integer.SIZE;
    private static HashMap<Integer, EotSFlagState> mappings;
    private int intValue;

    private EotSFlagState(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static HashMap<Integer, EotSFlagState> getMappings() {
        if (mappings == null) {
            synchronized (EotSFlagState.class) {
                if (mappings == null) {
                    mappings = new HashMap<Integer, EotSFlagState>();
                }
            }
        }
        return mappings;
    }

    public static EotSFlagState forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
