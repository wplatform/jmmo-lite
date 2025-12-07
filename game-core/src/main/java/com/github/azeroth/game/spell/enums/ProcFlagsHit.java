package com.github.azeroth.game.spell.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProcFlagsHit implements EnumFlag.FlagValue {
    NONE(0x0000000), // no second - NORMAL | CRITICAL for TAKEN proc type, NORMAL | CRITICAL | ABSORB for DONE
    NORMAL(0x0000001), // non-critical hits
    CRITICAL(0x0000002),
    MISS(0x0000004),
    FULL_RESIST(0x0000008),
    DODGE(0x0000010),
    PARRY(0x0000020),
    BLOCK(0x0000040), // partial or full block
    EVADE(0x0000080),
    IMMUNE(0x0000100),
    DEFLECT(0x0000200),
    ABSORB(0x0000400), // partial or full absorb
    REFLECT(0x0000800),
    INTERRUPT(0x0001000),
    FULL_BLOCK(0x0002000),
    MASK_ALL(0x0003FFF);

    public final int value;
}
