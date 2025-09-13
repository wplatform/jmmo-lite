package com.github.azeroth.game.domain.unit;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// byte (1 from 0..3) of UNIT_FIELD_BYTES_2
@Getter
@RequiredArgsConstructor
public enum UnitPVPStateFlag implements EnumFlag.FlagValue {
    UNIT_BYTE2_FLAG_NONE(0x00),
    UNIT_BYTE2_FLAG_PVP(0x01),
    UNIT_BYTE2_FLAG_UNK1(0x02),
    UNIT_BYTE2_FLAG_FFA_PVP(0x04),
    UNIT_BYTE2_FLAG_SANCTUARY(0x08),
    UNIT_BYTE2_FLAG_UNK4(0x10),
    UNIT_BYTE2_FLAG_UNK5(0x20),
    UNIT_BYTE2_FLAG_UNK6(0x40),
    UNIT_BYTE2_FLAG_UNK7(0x80);

    public final int value;
}
