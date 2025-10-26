package com.github.azeroth.auth.dto;

public enum AccountType {
    SEC_PLAYER         ,
    SEC_MODERATOR      ,
    SEC_GAME_MASTER,
    SEC_ADMINISTRATOR  ,
    SEC_CONSOLE                                  // must be always last in list, accounts must have less security level always also
}
