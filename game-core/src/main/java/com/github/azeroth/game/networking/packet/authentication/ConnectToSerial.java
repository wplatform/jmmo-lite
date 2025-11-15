package com.github.azeroth.game.networking.packet.authentication;

public enum ConnectToSerial {
    None            ,
    Realm           ,
    WorldAttempt1   ,
    WorldAttempt2   ,
    WorldAttempt3   ,
    WorldAttempt4   ,
    WorldAttempt5   ;

    public static ConnectToSerial valueOf(int value) {
        return switch (value) {
            case 0 -> None;
            case 14 -> Realm;
            case 17 -> WorldAttempt1;
            case 35 -> WorldAttempt2;
            case 53 -> WorldAttempt3;
            case 71 -> WorldAttempt4;
            case 89 -> WorldAttempt5;
            default -> throw new IllegalArgumentException("Unknown ConnectToSerial: " + value);
        };
    }
}
