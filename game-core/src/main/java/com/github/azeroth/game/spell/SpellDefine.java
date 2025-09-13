package com.github.azeroth.game.spell;

import com.badlogic.gdx.utils.IntIntMap;

public interface SpellDefine {


    int MAX_SPELLMOD = 40;


    int MELEE_BASED_TRIGGER_MASK = (procFlags.PROC_FLAG_DEAL_MELEE_SWING.value |
            procFlags.PROC_FLAG_TAKE_MELEE_SWING.value |
            procFlags.PROC_FLAG_DEAL_MELEE_ABILITY.value |
            procFlags.PROC_FLAG_TAKE_MELEE_ABILITY.value |
            procFlags.PROC_FLAG_DEAL_RANGED_ATTACK.value |
            procFlags.PROC_FLAG_TAKE_RANGED_ATTACK.value |
            procFlags.PROC_FLAG_DEAL_RANGED_ABILITY.value |
            procFlags.PROC_FLAG_TAKE_RANGED_ABILITY.value);

    ;;;;


    int PROC_ATTR_ALL_ALLOWED = (ProcAttributes.PROC_ATTR_REQ_EXP_OR_HONOR.value |
            ProcAttributes.PROC_ATTR_TRIGGERED_CAN_PROC.value |
            ProcAttributes.PROC_ATTR_REQ_POWER_COST.value |
            ProcAttributes.PROC_ATTR_REQ_SPELLMOD.value |
            ProcAttributes.PROC_ATTR_USE_STACKS_FOR_CHARGES.value |
            ProcAttributes.PROC_ATTR_REDUCE_PROC_60.value);
    ;;;;


    int SPELL_GROUP_DB_RANGE_MIN = 1000;


    ;;;;
    int SPELL_CHANNEL_UPDATE_INTERVAL = 1000;

    ;;;
    float MAX_SPELL_RANGE_TOLERANCE = 3.0f;
    float TRAJECTORY_MISSILE_SIZE = 3.0f;
    int AOE_DAMAGE_TARGET_CAP = 20;
    int SPELL_INTERRUPT_NONPLAYER = 32747;

    // Spell pet auras
    @Data
    class PetAura {
        IntIntMap auras;
        boolean removeOnChangePet;
        int damage;
    }

    // Spell rank chain  (accessed using SpellMgr functions)
    @Data
    class SpellChainNode {
        SpellInfo prev;
        SpellInfo next;
        SpellInfo first;
        SpellInfo last;
        byte rank;
    }


    ;;;;
}
