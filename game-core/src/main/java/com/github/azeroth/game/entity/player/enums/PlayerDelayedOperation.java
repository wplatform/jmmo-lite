package com.github.azeroth.game.entity.player.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlayerDelayedOperation implements EnumFlag.FlagValue {
    SAVE_PLAYER(0x01),
    RESURRECT_PLAYER(0x02),
    SPELL_CAST_DESERTER(0x04),
    BG_MOUNT_RESTORE(0x08),                     ///< Flag to restore mount state after teleport from BG
    BG_TAXI_RESTORE(0x10),                     ///< Flag to restore taxi state after teleport from BG
    BG_GROUP_RESTORE(0x20);                     ///< Flag to restore group state after teleport from BG
    public final int value;
}
