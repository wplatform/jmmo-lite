package com.github.azeroth.game.networking.packet.collection;

public enum CollectionType {
    NONE(-1),
    Toybox(1),
    Appearance(3),
    TransmogSet(4);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, CollectionType> mappings;
    private int intValue;

    private CollectionType(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, CollectionType> getMappings() {
        if (mappings == null) {
            synchronized (CollectionType.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, CollectionType>();
                }
            }
        }
        return mappings;
    }

    public static CollectionType forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
