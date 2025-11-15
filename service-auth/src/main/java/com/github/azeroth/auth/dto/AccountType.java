package com.github.azeroth.auth.dto;

import java.util.Objects;

public enum AccountType {
    SEC_PLAYER,
    SEC_MODERATOR,
    SEC_GAME_MASTER,
    SEC_ADMINISTRATOR,
    SEC_CONSOLE;                                  // must be always last in list, accounts must have less security level always also

    public static AccountType indexOf(int securityLevel) {
        Objects.checkFromIndexSize(securityLevel, 0, values().length);
        return AccountType.values()[securityLevel];
    }
}
