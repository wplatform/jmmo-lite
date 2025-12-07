package com.github.azeroth.game.spell.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProcFlagsSpellType {
    NONE(0x0000000),
    DAMAGE(0x0000001), // damage type of spell
    HEAL(0x0000002), // heal type of spell
    NO_DMG_HEAL(0x0000004), // other spells
    MASK_ALL(DAMAGE.value | HEAL.value | NO_DMG_HEAL.value);
    public final int value;
}
