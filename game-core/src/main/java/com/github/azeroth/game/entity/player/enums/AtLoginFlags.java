package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

// 2^n values
@RequiredArgsConstructor
public
enum AtLoginFlags {
    NONE(0x000),
    RENAME(0x001),
    RESET_SPELLS(0x002),
    RESET_TALENTS(0x004),
    CUSTOMIZE(0x008),
    RESET_PET_TALENTS(0x010),
    FIRST(0x020),
    CHANGE_FACTION(0x040),
    CHANGE_RACE(0x080),
    RESURRECT(0x100);

    public final int value;
}
