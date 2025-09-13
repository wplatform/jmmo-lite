package com.github.azeroth.game.battleground.zones;


import java.util.HashMap;

enum ABNodeStatus {
    Neutral(0),
    Contested(1),
    AllyContested(1),
    HordeContested(2),
    Occupied(3),
    AllyOccupied(3),
    HordeOccupied(4);

    public static final int SIZE = Integer.SIZE;
    private static HashMap<Integer, ABNodeStatus> mappings;
    private int intValue;

    private ABNodeStatus(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static HashMap<Integer, ABNodeStatus> getMappings() {
        if (mappings == null) {
            synchronized (ABNodeStatus.class) {
                if (mappings == null) {
                    mappings = new HashMap<Integer, ABNodeStatus>();
                }
            }
        }
        return mappings;
    }

    public static ABNodeStatus forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
