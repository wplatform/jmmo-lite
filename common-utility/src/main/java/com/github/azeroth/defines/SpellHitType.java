package com.github.azeroth.defines;

import com.github.azeroth.common.EnumFlag;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SpellHitType implements EnumFlag.FlagValue {
    CRIT_DEBUG(0x01),
    CRIT(0x02),
    HIT_DEBUG(0x04),
    SPLIT(0x08),
    VICTIM_IS_ATTACKER(0x10),
    ATTACK_TABLE_DEBUG(0x20),
    UNK(0x40),
    NO_ATTACKER(0x80); // does the same as SPELL_ATTR4_COMBAT_LOG_NO_CASTER

    public final int value;
}
