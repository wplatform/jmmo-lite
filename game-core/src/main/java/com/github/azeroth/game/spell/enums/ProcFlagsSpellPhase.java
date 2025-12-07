package com.github.azeroth.game.spell.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProcFlagsSpellPhase {
    NONE(0x0000000),
    CAST(0x0000001),
    HIT(0x0000002),
    FINISH(0x0000004),
    MASK_ALL(CAST.value | HIT.value | FINISH.value);

    public final int value;
}
