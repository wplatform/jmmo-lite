package com.github.azeroth.game.ai;

public enum AICondition {
    Aggro,
    Combat,
    Die;

    public static final int SIZE = Integer.SIZE;

    public static AICondition forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
