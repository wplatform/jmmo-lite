package com.github.azeroth.game.spell;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuraFlags implements EnumFlag.FlagValue {

    NONE(0x0000),
    NO_CASTER(0x0001),
    CANCELABLE(0x0002),
    DURATION(0x0004),
    SCALABLE(0x0008),
    NEGATIVE(0x0010),
    UNK20(0x0020),
    UNK40(0x0040),
    UNK80(0x0080),
    POSITIVE(0x0100),
    PASSIVE(0x0200);

    private final int value;
}
