package com.github.azeroth.game.spell.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProcAttributes {
    PROC_ATTR_NONE(0x0000000),
    PROC_ATTR_REQ_EXP_OR_HONOR(0x0000001), // requires proc target to give exp or honor for aura proc
    PROC_ATTR_TRIGGERED_CAN_PROC(0x0000002), // aura can proc even with triggered spells
    PROC_ATTR_REQ_POWER_COST(0x0000004), // requires triggering spell to have a power cost for aura proc
    PROC_ATTR_REQ_SPELLMOD(0x0000008), // requires triggering spell to be affected by proccing aura to drop charges
    PROC_ATTR_USE_STACKS_FOR_CHARGES(0x0000010), // consuming proc drops a stack from proccing aura instead of charge

    PROC_ATTR_REDUCE_PROC_60(0x0000080), // aura should have a reduced chance to proc if level of proc Actor > 60
    PROC_ATTR_CANT_PROC_FROM_ITEM_CAST(0x0000100); // do not allow aura proc if proc is caused by a spell casted by item

    public final int value;
}
