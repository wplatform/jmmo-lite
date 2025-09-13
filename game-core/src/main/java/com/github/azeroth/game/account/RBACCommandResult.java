package com.github.azeroth.game.account;

public enum RBACCommandResult {
    OK,
    CantAddAlreadyAdded,
    CantRevokeNotInList,
    InGrantedList,
    InDeniedList,
    IdDoesNotExists;

    public static final int SIZE = Integer.SIZE;

    public static RBACCommandResult forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
