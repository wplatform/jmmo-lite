package com.github.azeroth.game.spell.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProcFlagsSpellPhase {
    PROC_SPELL_PHASE_NONE(0x0000000),
    PROC_SPELL_PHASE_CAST(0x0000001),
    PROC_SPELL_PHASE_HIT(0x0000002),
    PROC_SPELL_PHASE_FINISH(0x0000004),
    PROC_SPELL_PHASE_MASK_ALL(PROC_SPELL_PHASE_CAST.value | PROC_SPELL_PHASE_HIT.value | PROC_SPELL_PHASE_FINISH.value);

    public final int value;
}
