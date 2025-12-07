package com.github.azeroth.defines;

public enum LootError {
    DIDNT_KILL               ,    // You don't have permission to loot that corpse.
    TOO_FAR                  ,    // You are too far away to loot that corpse.
    BAD_FACING               ,    // You must be facing the corpse to loot it.
    LOCKED                   ,    // Someone is already looting that corpse.
    NOT_STANDING              ,    // You need to be standing up to loot something!
    STUNNED                  ,    // You can't loot anything while stunned!
    PLAYER_NOT_FOUND         ,    // Player not found
    PLAY_TIME_EXCEEDED       ,    // Maximum play time exceeded
    MASTER_INV_FULL          ,    // That player's inventory is full
    MASTER_UNIQUE_ITEM       ,    // Player has too many of that item already
    MASTER_OTHER             ,    // Can't assign item to that player
    ALREADY_PICK_POCKETED     ,    // Your target has already had its pockets picked
    NOT_WHILE_SHAPESHIFTED   ,    // You can't do that while shapeshifted.
    NO_LOOT                      // There is no loot.
}
