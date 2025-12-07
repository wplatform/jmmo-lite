package com.github.azeroth.dbc.defines;

import com.github.azeroth.common.EnumFlag;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TaxiPathNodeFlag implements EnumFlag.FlagValue {
    TELEPORT(0x1),
    STOP(0x2);
    public final int value;
}
