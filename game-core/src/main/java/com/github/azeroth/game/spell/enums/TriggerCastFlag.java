package com.github.azeroth.game.spell.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TriggerCastFlag implements EnumFlag.FlagValue {
    NONE(0x00000000),   //!< Not triggered
    IGNORE_GCD(0x00000001),   //!< Will ignore GCD
    IGNORE_SPELL_AND_CATEGORY_CD(0x00000002),   //!< Will ignore Spell and Category cooldowns
    IGNORE_POWER_AND_REAGENT_COST(0x00000004),   //!< Will ignore power and reagent cost
    IGNORE_CAST_ITEM(0x00000008),   //!< Will not take away cast item or update related achievement criteria
    IGNORE_AURA_SCALING(0x00000010),   //!< Will ignore aura scaling
    IGNORE_CAST_IN_PROGRESS(0x00000020),   //!< Will not check if a current cast is in progress
    // reuse                                        = 0x00000040,
    CAST_DIRECTLY(0x00000080),   //!< In Spell::prepare, will be cast directly without setting containers for executed spell
    IGNORE_AURA_INTERRUPT_FLAGS(0x00000100),   //!< Will ignore interruptible aura's at cast
    IGNORE_SET_FACING(0x00000200),   //!< Will not adjust facing to target (if any)
    IGNORE_SHAPESHIFT(0x00000400),   //!< Will ignore shapeshift checks
    // reuse
    DISALLOW_PROC_EVENTS(0x00001000),   //!< Disallows proc events from triggered spell (default)
    IGNORE_CASTER_MOUNTED_OR_ON_VEHICLE(0x00002000),   //!< Will ignore mounted/on vehicle restrictions
    // reuse                                        = 0x00004000,
    // reuse                                        = 0x00008000,
    IGNORE_CASTER_AURAS(0x00010000),   //!< Will ignore caster aura restrictions or requirements
    DONT_RESET_PERIODIC_TIMER(0x00020000),   //!< Will allow periodic aura timers to keep ticking (instead of resetting)
    DONT_REPORT_CAST_ERROR(0x00040000),   //!< Will return SPELL_FAILED_DONT_REPORT in CheckCast functions
    FULL_MASK(0x0007FFFF),   //!< Used when doing CastSpell with triggered == true

    // debug flags (used with .cast triggered commands)
    IGNORE_EQUIPPED_ITEM_REQUIREMENT(0x00080000),   //!< Will ignore equipped item requirements
    IGNORE_TARGET_CHECK(0x00100000),   //!< Will ignore most target checks (mostly DBC target checks)
    IGNORE_CASTER_AURASTATE(0x00200000),   //!< Will ignore caster aura states including combat requirements and death state
    FULL_DEBUG_MASK(0xFFFFFFFF);

    public final int value;
}
