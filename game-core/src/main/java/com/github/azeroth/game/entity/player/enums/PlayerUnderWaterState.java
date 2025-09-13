package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

// 2^n values, Player::m_isunderwater is a bitmask. These are Trinity internal values, they are never send to any client
@RequiredArgsConstructor
public
enum PlayerUnderWaterState {
    NONE(0x00),
    IN_WATER(0x01),             // terrain type is water and player is afflicted by it
    IN_LAVA(0x02),             // terrain type is lava and player is afflicted by it
    IN_SLIME(0x04),             // terrain type is lava and player is afflicted by it
    IN_DARK_WATER(0x08),             // terrain type is dark water and player is afflicted by it

    EXIST_TIMERS(0x10);
    public final int value;
}
