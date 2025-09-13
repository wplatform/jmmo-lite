package com.github.azeroth.game.spell.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpellInterruptFlag implements EnumFlag.FlagValue {
    NONE(0),
    movement(0x00000001),
    DamagePushbackPlayerOnly(0x00000002),
    Stun(0x00000004), // useless, even spells without it get interrupted
    Combat(0x00000008),
    DamageCancelsPlayerOnly(0x00000010),
    MeleeCombat(0x00000020), // NYI
    Immunity(0x00000040), // NYI
    DamageAbsorb(0x00000080),
    ZeroDamageCancels(0x00000100),
    DamagePushback(0x00000200),
    DamageCancels(0x00000400);

    public final int value;
}
