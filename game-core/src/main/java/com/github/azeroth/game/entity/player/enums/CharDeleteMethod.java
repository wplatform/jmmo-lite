package com.github.azeroth.game.entity.player.enums;

public enum CharDeleteMethod {
    CHAR_DELETE_REMOVE,                      // Completely remove from the database
    CHAR_DELETE_UNLINK                       // The character gets unlinked from the account,
    // the name gets freed up and appears as deleted ingame
}
