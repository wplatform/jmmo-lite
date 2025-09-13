package com.github.azeroth.game.entity.player.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.RequiredArgsConstructor;
import lombok.Getter;

@Getter
@RequiredArgsConstructor
public enum PlayerCommandState implements EnumFlag.FlagValue {
    NONE(0x00),
    GOD(0x01),
    CAST_TIME(0x02),
    COOLDOWN(0x04),
    POWER(0x08),
    WATER_WALK(0x10);

    public final int value;
}
