package com.github.azeroth.game.domain.unit;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter(onMethod = @__({@Override}))
@RequiredArgsConstructor
public enum UnitTypeMask implements EnumFlag.FlagValue {
    NONE(0x00000000),
    SUMMON(0x00000001),
    MINION(0x00000002),
    GUARDIAN(0x00000004),
    TOTEM(0x00000008),
    PET(0x00000010),
    VEHICLE(0x00000020),
    PUPPET(0x00000040),
    HUNTER_PET(0x00000080),
    CONTROLLABLE_GUARDIAN(0x00000100),
    ACCESSORY(0x00000200);

    public final int value;
}
