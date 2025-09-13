package com.github.azeroth.game.battleground.zones;


import java.util.HashMap;

enum ABObjectives {
    AssaultBase(122),
    DefendBase(123);

    public static final int SIZE = Integer.SIZE;
    private static HashMap<Integer, ABObjectives> mappings;
    private int intValue;

    private ABObjectives(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static HashMap<Integer, ABObjectives> getMappings() {
        if (mappings == null) {
            synchronized (ABObjectives.class) {
                if (mappings == null) {
                    mappings = new HashMap<Integer, ABObjectives>();
                }
            }
        }
        return mappings;
    }

    public static ABObjectives forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}


///#endregion

