package com.github.azeroth.game.spell.auras.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AuraFlags {
    AFLAG_NONE(0x0000),
    AFLAG_NOCASTER(0x0001),
    AFLAG_POSITIVE(0x0002),
    AFLAG_DURATION(0x0004),
    AFLAG_SCALABLE(0x0008),
    AFLAG_NEGATIVE(0x0010),
    AFLAG_UNK20(0x0020),
    AFLAG_UNK40(0x0040),
    AFLAG_UNK80(0x0080),
    AFLAG_MAW_POWER(0x0100);

    public final int value;
}
