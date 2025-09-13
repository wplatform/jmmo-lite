package com.github.azeroth.game.spell.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpellAuraInterruptFlag implements EnumFlag.FlagValue {
    NONE(0),
    HostileActionReceived(0x00000001),
    damage(0x00000002),
    action(0x00000004),
    Moving(0x00000008),
    Turning(0x00000010),
    Anim(0x00000020),
    dismount(0x00000040),
    UnderWater(0x00000080), // TODO: disallow casting when swimming (SPELL_FAILED_ONLY_ABOVEWATER)
    AboveWater(0x00000100), // TODO: disallow casting when not swimming (SPELL_FAILED_ONLY_UNDERWATER)
    Sheathing(0x00000200),
    Interacting(0x00000400), // TODO: more than gossip, replace all the feign death removals by aura type
    Looting(0x00000800),
    attacking(0x00001000),
    ItemUse(0x00002000),
    DamageChannelDuration(0x00004000),
    Shapeshifting(0x00008000),
    ActionDelayed(0x00010000),
    mount(0x00020000),
    standing(0x00040000),
    LeaveWorld(0x00080000),
    StealthOrInvis(0x00100000),
    InvulnerabilityBuff(0x00200000),
    EnterWorld(0x00400000),
    PvPActive(0x00800000),
    NonPeriodicDamage(0x01000000),
    LandingOrFlight(0x02000000),
    Release(0x04000000),
    DamageCancelsScript(0x08000000), // NYI dedicated aura script hook
    EnteringCombat(0x10000000),
    Login(0x20000000),
    summon(0x40000000),
    LeavingCombat(0x80000000),

    NOT_VICTIM(HostileActionReceived.value | damage.value | NonPeriodicDamage.value);

    public final int value;
}
