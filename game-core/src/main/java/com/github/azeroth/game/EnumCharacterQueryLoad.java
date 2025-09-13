package com.github.azeroth.game;


enum EnumCharacterQueryLoad {
    characters,
    customizations;

    public static final int SIZE = Integer.SIZE;

    public static EnumCharacterQueryLoad forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
