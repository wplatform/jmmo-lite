package com.github.azeroth.game.spell;

public enum AuraObjectType {
    unit,
    DynObj;

    public static final int SIZE = Integer.SIZE;

    public static AuraObjectType forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
