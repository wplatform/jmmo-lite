package com.github.azeroth.game.spell.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SpellAreaFlag {
    SPELL_AREA_FLAG_AUTOCAST(0x1), // if has autocast, spell is applied on enter
    SPELL_AREA_FLAG_AUTOREMOVE(0x2), // if has autoremove, spell is remove automatically inside zone/area (always removed on leaving area or zone)
    SPELL_AREA_FLAG_IGNORE_AUTOCAST_ON_QUEST_STATUS_CHANGE(0x4); // if this flag is set then spell will not be applied automatically on quest status change

    public final int value;
}
