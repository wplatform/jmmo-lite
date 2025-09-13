package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public
enum PlayerDelayedOperation {
    DELAYED_SAVE_PLAYER(0x01),
    DELAYED_RESURRECT_PLAYER(0x02),
    DELAYED_SPELL_CAST_DESERTER(0x04),
    DELAYED_BG_MOUNT_RESTORE(0x08),                     ///< Flag to restore mount state after teleport from BG
    DELAYED_BG_TAXI_RESTORE(0x10),                     ///< Flag to restore taxi state after teleport from BG
    DELAYED_BG_GROUP_RESTORE(0x20);                     ///< Flag to restore group state after teleport from BG
    public final int value;
}
