package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

// 2^n values
@RequiredArgsConstructor
public
enum AtLoginFlags {
    AT_LOGIN_NONE(0x000),
    AT_LOGIN_RENAME(0x001),
    AT_LOGIN_RESET_SPELLS(0x002),
    AT_LOGIN_RESET_TALENTS(0x004),
    AT_LOGIN_CUSTOMIZE(0x008),
    AT_LOGIN_RESET_PET_TALENTS(0x010),
    AT_LOGIN_FIRST(0x020),
    AT_LOGIN_CHANGE_FACTION(0x040),
    AT_LOGIN_CHANGE_RACE(0x080),
    AT_LOGIN_RESURRECT(0x100);

    public final int value;
}
