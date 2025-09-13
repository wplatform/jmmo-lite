package com.github.azeroth.game.entity.player;

public enum SocialFlag {
    Friend(0x01),
    Ignored(0x02),
    Muted(0x04), // guessed
    unk(0x08), // Unknown - does not appear to be RaF
    All(0x01 | 0x02 | 0x04);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, SocialFlag> mappings;
    private int intValue;

    private SocialFlag(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, SocialFlag> getMappings() {
        if (mappings == null) {
            synchronized (SocialFlag.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, SocialFlag>();
                }
            }
        }
        return mappings;
    }

    public static SocialFlag forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
