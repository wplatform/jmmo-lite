package com.github.azeroth.game.spell;

public enum SpellEffectHandleMode {
    Launch,
    LaunchTarget,
    hit,
    HitTarget;

    public static final int SIZE = Integer.SIZE;

    public static SpellEffectHandleMode forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
