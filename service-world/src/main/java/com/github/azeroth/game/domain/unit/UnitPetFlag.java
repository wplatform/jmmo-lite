package com.github.azeroth.game.domain.unit;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// byte (2 from 0..3) of UNIT_FIELD_BYTES_2
@Getter
@RequiredArgsConstructor
public enum UnitPetFlag implements EnumFlag.FlagValue {
    UNIT_PET_FLAG_NONE(0x00),
    UNIT_PET_FLAG_CAN_BE_RENAMED(0x01),
    UNIT_PET_FLAG_CAN_BE_ABANDONED(0x02);

    public final int value;
}
