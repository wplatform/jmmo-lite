package com.github.azeroth.game.spell.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProcFlagsSpellType {
    PROC_SPELL_TYPE_NONE(0x0000000),
    PROC_SPELL_TYPE_DAMAGE(0x0000001), // damage type of spell
    PROC_SPELL_TYPE_HEAL(0x0000002), // heal type of spell
    PROC_SPELL_TYPE_NO_DMG_HEAL(0x0000004), // other spells
    PROC_SPELL_TYPE_MASK_ALL(PROC_SPELL_TYPE_DAMAGE.value | PROC_SPELL_TYPE_HEAL.value | PROC_SPELL_TYPE_NO_DMG_HEAL.value);

    public final int value;
}
