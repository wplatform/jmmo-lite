package com.github.azeroth.game.movement.enums;

public enum PolyFlag {
    Walk(1),
    swim(2);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, PolyFlag> mappings;
    private final int intValue;

    PolyFlag(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, PolyFlag> getMappings() {
        if (mappings == null) {
            synchronized (PolyFlag.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, PolyFlag>();
                }
            }
        }
        return mappings;
    }

    public static PolyFlag forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
