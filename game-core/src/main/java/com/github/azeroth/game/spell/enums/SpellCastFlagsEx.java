package com.github.azeroth.game.spell.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpellCastFlagsEx implements EnumFlag.FlagValue {
    CAST_FLAG_EX_NONE(0x00000),
    CAST_FLAG_EX_TRIGGER_COOLDOWN_ON_SPELL_START(0x00001),
    CAST_FLAG_EX_UNKNOWN_2(0x00002),
    CAST_FLAG_EX_DONT_CONSUME_CHARGES(0x00004),
    CAST_FLAG_EX_UNKNOWN_4(0x00008),
    CAST_FLAG_EX_DELAY_STARTING_COOLDOWNS(0x00010),  // makes client start cooldown after precalculated delay instead of immediately after SPELL_GO (used by empower spells)
    CAST_FLAG_EX_UNKNOWN_6(0x00020),
    CAST_FLAG_EX_UNKNOWN_7(0x00040),
    CAST_FLAG_EX_UNKNOWN_8(0x00080),
    CAST_FLAG_EX_IGNORE_PET_COOLDOWN(0x00100),  // makes client not automatically start cooldown for pets after SPELL_GO
    CAST_FLAG_EX_IGNORE_COOLDOWN(0x00200),  // makes client not automatically start cooldown after SPELL_GO
    CAST_FLAG_EX_UNKNOWN_11(0x00400),
    CAST_FLAG_EX_UNKNOWN_12(0x00800),
    CAST_FLAG_EX_UNKNOWN_13(0x01000),
    CAST_FLAG_EX_UNKNOWN_14(0x02000),
    CAST_FLAG_EX_UNKNOWN_15(0x04000),
    CAST_FLAG_EX_USE_TOY_SPELL(0x08000),  // Starts cooldown on toy
    CAST_FLAG_EX_UNKNOWN_17(0x10000),
    CAST_FLAG_EX_UNKNOWN_18(0x20000),
    CAST_FLAG_EX_UNKNOWN_19(0x40000),
    CAST_FLAG_EX_UNKNOWN_20(0x80000);

    public final int value;
}
