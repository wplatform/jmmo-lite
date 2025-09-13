package com.github.azeroth.game.spell.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public

// Spell proc event related declarations (accessed using SpellMgr functions)
enum ProcFlags implements EnumFlag.FlagValue {
    NONE(0x00000000),
    HEARTBEAT(0x00000001),    // 00 Killed by agressor - not sure about this flag
    KILL(0x00000002),    // 01 Kill target (in most cases need XP/Honor reward)
    DEAL_MELEE_SWING(0x00000004),    // 02 Done melee auto attack
    TAKE_MELEE_SWING(0x00000008),    // 03 Taken melee auto attack
    DEAL_MELEE_ABILITY(0x00000010),    // 04 Done attack by Spell that has dmg class melee
    TAKE_MELEE_ABILITY(0x00000020),    // 05 Taken attack by Spell that has dmg class melee
    DEAL_RANGED_ATTACK(0x00000040),    // 06 Done ranged auto attack
    TAKE_RANGED_ATTACK(0x00000080),    // 07 Taken ranged auto attack
    DEAL_RANGED_ABILITY(0x00000100),    // 08 Done attack by Spell that has dmg class ranged
    TAKE_RANGED_ABILITY(0x00000200),    // 09 Taken attack by Spell that has dmg class ranged
    DEAL_HELPFUL_ABILITY(0x00000400),    // 10 Done positive spell that has dmg class none
    TAKE_HELPFUL_ABILITY(0x00000800),    // 11 Taken positive spell that has dmg class none
    DEAL_HARMFUL_ABILITY(0x00001000),    // 12 Done negative spell that has dmg class none
    TAKE_HARMFUL_ABILITY(0x00002000),    // 13 Taken negative spell that has dmg class none
    DEAL_HELPFUL_SPELL(0x00004000),    // 14 Done positive spell that has dmg class magic
    TAKE_HELPFUL_SPELL(0x00008000),    // 15 Taken positive spell that has dmg class magic
    DEAL_HARMFUL_SPELL(0x00010000),    // 16 Done negative spell that has dmg class magic
    TAKE_HARMFUL_SPELL(0x00020000),    // 17 Taken negative spell that has dmg class magic
    DEAL_HARMFUL_PERIODIC(0x00040000),    // 18 Successful do periodic (damage)
    TAKE_HARMFUL_PERIODIC(0x00080000),    // 19 Taken spell periodic (damage)
    TAKE_ANY_DAMAGE(0x00100000),    // 20 Taken any damage
    DEAL_HELPFUL_PERIODIC(0x00200000),    // 21 On trap activation (possibly needs name change to ON_GAMEOBJECT_CAST or USE)
    MAIN_HAND_WEAPON_SWING(0x00400000),    // 22 Done main-hand melee attacks (spell and autoattack)
    OFF_HAND_WEAPON_SWING(0x00800000),    // 23 Done off-hand melee attacks (spell and autoattack)
    DEATH(0x01000000),    // 24 Died in any way
    JUMP(0x02000000),    // 25 Jumped
    PROC_CLONE_SPELL(0x04000000),    // 26 Proc Clone Spell
    ENTER_COMBAT(0x08000000),    // 27 Entered combat
    ENCOUNTER_START(0x10000000),    // 28 Encounter started
    CAST_ENDED(0x20000000),    // 29 Cast Ended
    LOOTED(0x40000000),    // 30 Looted (took from loot, not opened loot window)
    TAKE_HELPFUL_PERIODIC(0x80000000),    // 31 Take Helpful Periodic

    // flag masks
    AUTO_ATTACK_MASK(DEAL_MELEE_SWING.value | TAKE_MELEE_SWING.value
            | DEAL_RANGED_ATTACK.value | TAKE_RANGED_ATTACK.value),

    MELEE_MASK(DEAL_MELEE_SWING.value | TAKE_MELEE_SWING.value
            | DEAL_MELEE_ABILITY.value | TAKE_MELEE_ABILITY.value
            | MAIN_HAND_WEAPON_SWING.value | OFF_HAND_WEAPON_SWING.value),

    RANGED_MASK(DEAL_RANGED_ATTACK.value | TAKE_RANGED_ATTACK.value
            | DEAL_RANGED_ABILITY.value | TAKE_RANGED_ABILITY.value),

    SPELL_MASK(DEAL_MELEE_ABILITY.value | TAKE_MELEE_ABILITY.value
            | DEAL_RANGED_ATTACK.value | TAKE_RANGED_ATTACK.value
            | DEAL_RANGED_ABILITY.value | TAKE_RANGED_ABILITY.value
            | DEAL_HELPFUL_ABILITY.value | TAKE_HELPFUL_ABILITY.value
            | DEAL_HARMFUL_ABILITY.value | TAKE_HARMFUL_ABILITY.value
            | DEAL_HELPFUL_SPELL.value | TAKE_HELPFUL_SPELL.value
            | DEAL_HARMFUL_SPELL.value | TAKE_HARMFUL_SPELL.value
            | DEAL_HARMFUL_PERIODIC.value | TAKE_HARMFUL_PERIODIC.value
            | DEAL_HELPFUL_PERIODIC.value | TAKE_HELPFUL_PERIODIC.value),

    DONE_HIT_MASK(DEAL_MELEE_SWING.value | DEAL_RANGED_ATTACK.value
            | DEAL_MELEE_ABILITY.value | DEAL_RANGED_ABILITY.value
            | DEAL_HELPFUL_ABILITY.value | DEAL_HARMFUL_ABILITY.value
            | DEAL_HELPFUL_SPELL.value | DEAL_HARMFUL_SPELL.value
            | DEAL_HARMFUL_PERIODIC.value | DEAL_HELPFUL_PERIODIC.value
            | MAIN_HAND_WEAPON_SWING.value | OFF_HAND_WEAPON_SWING.value),

    TAKEN_HIT_MASK(TAKE_MELEE_SWING.value | TAKE_RANGED_ATTACK.value
            | TAKE_MELEE_ABILITY.value | TAKE_RANGED_ABILITY.value
            | TAKE_HELPFUL_ABILITY.value | TAKE_HARMFUL_ABILITY.value
            | TAKE_HELPFUL_SPELL.value | TAKE_HARMFUL_SPELL.value
            | TAKE_HARMFUL_PERIODIC.value | TAKE_HELPFUL_PERIODIC.value
            | TAKE_ANY_DAMAGE.value),

    REQ_SPELL_PHASE_MASK(SPELL_MASK.value & DONE_HIT_MASK.value);

    public final int value;
}
