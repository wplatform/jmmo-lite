package com.github.azeroth.game.text;

public enum SoundKitPlayType {
    NORMAL(0),
    ObjectSound(1),
    max(2);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, SoundKitPlayType> mappings;
    private int intValue;

    private SoundKitPlayType(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, SoundKitPlayType> getMappings() {
        if (mappings == null) {
            synchronized (SoundKitPlayType.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, SoundKitPlayType>();
                }
            }
        }
        return mappings;
    }

    public static SoundKitPlayType forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
