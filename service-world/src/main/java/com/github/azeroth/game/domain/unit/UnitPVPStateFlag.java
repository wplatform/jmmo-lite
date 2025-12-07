package com.github.azeroth.game.domain.unit;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// byte (1 from 0..3) of UNIT_FIELD_BYTES_2
@Getter
@RequiredArgsConstructor
public enum UnitPVPStateFlag implements EnumFlag.FlagValue {
    NONE(0x00),
    PVP(0x01),
    UNK1(0x02),
    FFA_PVP(0x04),
    SANCTUARY(0x08),
    UNK4(0x10),
    UNK5(0x20),
    UNK6(0x40),
    UNK7(0x80);

    public final int value;
}
