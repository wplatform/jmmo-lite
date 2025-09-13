package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

// 2^n values
@RequiredArgsConstructor
public
enum PlayerExtraFlags {
    // gm abilities
    PLAYER_EXTRA_GM_ON(0x0001),
    PLAYER_EXTRA_ACCEPT_WHISPERS(0x0004),
    PLAYER_EXTRA_TAXICHEAT(0x0008),
    PLAYER_EXTRA_GM_INVISIBLE(0x0010),
    PLAYER_EXTRA_GM_CHAT(0x0020),       // Show GM badge in chat messages

    // other states
    PLAYER_EXTRA_PVP_DEATH(0x0100),       // store PvP death status until corpse creating.

    // Character services markers
    PLAYER_EXTRA_HAS_RACE_CHANGED(0x0200),
    PLAYER_EXTRA_GRANTED_LEVELS_FROM_RAF(0x0400),
    PLAYER_EXTRA_LEVEL_BOOSTED(0x0800);
    public final int value;
}
