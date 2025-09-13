package com.github.azeroth.game.entity.player;

enum FavoriteAppearanceState {
    New,
    removed,
    Unchanged;

    public static final int SIZE = Integer.SIZE;

    public static FavoriteAppearanceState forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
