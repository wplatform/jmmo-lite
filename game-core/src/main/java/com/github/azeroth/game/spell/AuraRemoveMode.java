package com.github.azeroth.game.spell;

public enum AuraRemoveMode {
    NONE(0),
    Default(1), // scripted remove, remove by stack with aura with different ids and sc aura remove
    interrupt(2),
    cancel(3),
    EnemySpell(4), // dispel and absorb aura destroy
    Expire(5), // aura duration has ended
    Death(6);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, AuraRemoveMode> mappings;
    private int intValue;

    private AuraRemoveMode(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, AuraRemoveMode> getMappings() {
        if (mappings == null) {
            synchronized (AuraRemoveMode.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, AuraRemoveMode>();
                }
            }
        }
        return mappings;
    }

    public static AuraRemoveMode forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
