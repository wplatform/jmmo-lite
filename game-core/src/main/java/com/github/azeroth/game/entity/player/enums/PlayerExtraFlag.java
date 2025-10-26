package com.github.azeroth.game.entity.player.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 2^n values
@Getter
@RequiredArgsConstructor
public enum PlayerExtraFlag implements EnumFlag.FlagValue {
    // gm abilities
    GM_ON(0x0001),
    ACCEPT_WHISPERS(0x0004),
    TAXI_CHEAT(0x0008),
    GM_INVISIBLE(0x0010),
    GM_CHAT(0x0020),       // Show GM badge in chat messages

    // other states
    PVP_DEATH(0x0100),       // store PvP death status until corpse creating.

    // Character services markers
    HAS_RACE_CHANGED(0x0200),
    GRANTED_LEVELS_FROM_RAF(0x0400),
    LEVEL_BOOSTED(0x0800);
    public final int value;
}
