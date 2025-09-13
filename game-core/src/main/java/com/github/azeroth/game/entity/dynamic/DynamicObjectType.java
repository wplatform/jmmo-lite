package com.github.azeroth.game.entity.dynamic;

public enum DynamicObjectType {
    Portal(0x0), // unused
    AreaSpell(0x1),
    FarsightFocus(0x2);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, DynamicObjectType> mappings;
    private int intValue;

    private DynamicObjectType(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, DynamicObjectType> getMappings() {
        if (mappings == null) {
            synchronized (DynamicObjectType.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, DynamicObjectType>();
                }
            }
        }
        return mappings;
    }

    public static DynamicObjectType forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
