package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

// used in PLAYER_FIELD_BYTES2 values
@RequiredArgsConstructor
public
enum PlayerFieldByte2Flag {
    NONE(0x00),
    STEALTH(0x20),
    INVISIBILITY_GLOW(0x40);
    public final int value;
}
