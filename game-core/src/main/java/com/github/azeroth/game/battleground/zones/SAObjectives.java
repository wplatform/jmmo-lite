package com.github.azeroth.game.battleground.zones;


import java.util.HashMap;

enum SAObjectives {
    gatesDestroyed(231),
    demolishersDestroyed(232);

    public static final int SIZE = Integer.SIZE;
    private static HashMap<Integer, SAObjectives> mappings;
    private int intValue;

    private SAObjectives(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static HashMap<Integer, SAObjectives> getMappings() {
        if (mappings == null) {
            synchronized (SAObjectives.class) {
                if (mappings == null) {
                    mappings = new HashMap<Integer, SAObjectives>();
                }
            }
        }
        return mappings;
    }

    public static SAObjectives forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}


///#endregion

