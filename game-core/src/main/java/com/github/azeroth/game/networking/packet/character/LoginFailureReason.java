package com.github.azeroth.game.networking.packet.character;

public enum LoginFailureReason {

    FAILED(0),
    NO_WORLD(1),
    DUPLICATE_CHARACTER(2),
    NO_INSTANCES(3),
    DISABLED (4),
    NO_CHARACTER (5),
    LOCKED_FOR_TRANSFER (6),
    LOCKED_BY_BILLING (7),
    LOCKED_BY_MOBILE_AH (8),
    TEMPORARY_GM_LOCK (9),
    LOCKED_BY_CHARACTER_UPGRADE (10),
    LOCKED_BY_REVOKED_CHARACTER_UPGRADE(11),
    LOCKED_BY_REVOKED_VAS_TRANSACTION (17),
    LOCKED_BY_RESTRICTION (19),
    LOCKED_FOR_REALM_PLAY_TYPE (23);


    public final byte code;

    LoginFailureReason(int code) {
        this.code = (byte) code;
    }
}
