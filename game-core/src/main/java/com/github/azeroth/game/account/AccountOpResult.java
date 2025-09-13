package com.github.azeroth.game.account;

public enum AccountOpResult {
    Ok,
    NameTooLong,
    PassTooLong,
    EmailTooLong,
    NameAlreadyExist,
    NameNotExist,
    DBInternalError,
    BadLink;

    public static final int SIZE = Integer.SIZE;

    public static AccountOpResult forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
