package com.github.azeroth.game.movement.model;


public enum EvaluationMode {
    Linear,
    Catmullrom,
    Bezier3_Unused,
    UninitializedMode,
    ModesEnd;

    public static final int SIZE = Integer.SIZE;

    public static EvaluationMode forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
