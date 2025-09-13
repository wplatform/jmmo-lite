package com.github.azeroth.game.battleground.zones;


import java.util.HashMap;

enum SAStatus {
    NotStarted(0),
    Warmup(1),
    RoundOne(2),
    SecondWarmup(3),
    RoundTwo(4),
    BonusRound(5);

    public static final int SIZE = Integer.SIZE;
    private static HashMap<Integer, SAStatus> mappings;
    private int intValue;

    private SAStatus(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static HashMap<Integer, SAStatus> getMappings() {
        if (mappings == null) {
            synchronized (SAStatus.class) {
                if (mappings == null) {
                    mappings = new HashMap<Integer, SAStatus>();
                }
            }
        }
        return mappings;
    }

    public static SAStatus forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
