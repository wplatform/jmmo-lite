package com.github.azeroth.game.domain.unit;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Value masks for UNIT_FIELD_FLAGS
// EnumUtils: DESCRIBE THIS
@Getter
@RequiredArgsConstructor
public enum UnitFlag implements EnumFlag.FlagValue {
    SERVER_CONTROLLED(0x00000001),           // set only when unit movement is controlled by server - by SPLINE/MONSTER_MOVE packets, together with STUNNED; only set to units controlled by client; client function CGUnit_C::IsClientControlled returns false when set for owner
    NON_ATTACKABLE(0x00000002),           // not attackable, set when creature starts to cast spells with SPELL_EFFECT_SPAWN and cast time, removed when spell hits caster, original name is SPAWNING. Rename when it will be removed from all scripts
    REMOVE_CLIENT_CONTROL(0x00000004),           // This is a legacy flag used to disable movement player's movement while controlling other units, SMSG_CLIENT_CONTROL replaces this functionality clientside now. CONFUSED and FLEEING flags have the same effect on client movement asDISABLE_MOVE_CONTROL in addition to preventing spell casts/autoattack (they all allow climbing steeper hills and emotes while moving)
    PLAYER_CONTROLLED(0x00000008),           // controlled by player, use _IMMUNE_TO_PC instead of _IMMUNE_TO_NPC
    RENAME(0x00000010),
    PREPARATION(0x00000020),           // don't take reagents for spells with SPELL_ATTR5_NO_REAGENT_WHILE_PREP
    UNK_6(0x00000040),
    NOT_ATTACKABLE_1(0x00000080),           // ?? (PLAYER_CONTROLLED | NOT_ATTACKABLE_1) is NON_PVP_ATTACKABLE
    IMMUNE_TO_PC(0x00000100),           // disables combat/assistance with PlayerCharacters (PC) - see Unit::IsValidAttackTarget, Unit::IsValidAssistTarget
    IMMUNE_TO_NPC(0x00000200),           // disables combat/assistance with NonPlayerCharacters (NPC) - see Unit::IsValidAttackTarget, Unit::IsValidAssistTarget
    LOOTING(0x00000400),           // loot animation
    PET_IN_COMBAT(0x00000800),           // on player pets: whether the pet is chasing a target to attack || on other units: whether any of the unit's minions is in combat
    PVP_ENABLING(0x00001000),           // changed in 3.0.3, now UNIT_BYTES_2_OFFSET_PVP_FLAG from UNIT_FIELD_BYTES_2
    SILENCED(0x00002000),           // silenced, 2.1.1
    CANT_SWIM(0x00004000),           // TITLE Can't Swim
    CAN_SWIM(0x00008000),           // TITLE Can Swim DESCRIPTION shows swim animation in water
    NON_ATTACKABLE_2(0x00010000),           // removes attackable icon, if on yourself, cannot assist self but can cast TARGET_SELF spells - added by SPELL_AURA_MOD_UNATTACKABLE
    PACIFIED(0x00020000),           // 3.0.3 ok
    STUNNED(0x00040000),           // 3.0.3 ok
    IN_COMBAT(0x00080000),
    ON_TAXI(0x00100000),           // disable casting at client side spell not allowed by taxi flight (mounted?), probably used with 0x4 flag
    DISARMED(0x00200000),           // 3.0.3, disable melee spells casting..., "Required melee weapon" added to melee spells tooltip.
    CONFUSED(0x00400000),
    FLEEING(0x00800000),
    POSSESSED(0x01000000),           // under direct client control by a player (possess or vehicle)
    UNINTERACTIBLE(0x02000000),
    SKINNABLE(0x04000000),
    MOUNT(0x08000000),
    UNK_28(0x10000000),
    PREVENT_EMOTES_FROM_CHAT_TEXT(0x20000000),   // Prevent automatically playing emotes from parsing chat text, for example "lol" in /say, ending message with ? or !, or using /yell
    SHEATHE(0x40000000),
    IMMUNE(0x80000000),           // Immune to damage

    DISALLOWED(SERVER_CONTROLLED.value | NON_ATTACKABLE.value | REMOVE_CLIENT_CONTROL.value |
            PLAYER_CONTROLLED.value | RENAME.value | PREPARATION.value | /* UNK_6.value | */
            NOT_ATTACKABLE_1.value | LOOTING.value | PET_IN_COMBAT.value | PVP_ENABLING.value |
            SILENCED.value | NON_ATTACKABLE_2.value | PACIFIED.value | STUNNED.value |
            IN_COMBAT.value | ON_TAXI.value | DISARMED.value | CONFUSED.value | FLEEING.value |
            POSSESSED.value | SKINNABLE.value | MOUNT.value | UNK_28.value |
            PREVENT_EMOTES_FROM_CHAT_TEXT.value | SHEATHE.value | IMMUNE.value), // SKIP

    ALLOWED(~DISALLOWED.value); // SKIP

    public final int value;
}
