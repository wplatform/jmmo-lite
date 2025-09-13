package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public
enum RuneCooldown {
    BASE_COOLDOWN(10000),
    MISS_COOLDOWN(1500);     // cooldown applied on runes when the spell misses
    public final int value;
}
