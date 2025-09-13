package com.github.azeroth.game.ai;

public enum AITarget {
    Self,
    victim,
    Enemy,
    Ally,
    Buff,
    Debuff;

    public static final int SIZE = Integer.SIZE;

    public static AITarget forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
