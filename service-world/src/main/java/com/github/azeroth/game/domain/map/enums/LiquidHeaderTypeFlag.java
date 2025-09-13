package com.github.azeroth.game.domain.map.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum LiquidHeaderTypeFlag implements EnumFlag.FlagValue {

    NoWater((byte) 0x00),
    Water((byte) 0x01),
    Ocean((byte) 0x02),
    Magma((byte) 0x04),
    Slime((byte) 0x08),
    DarkWater((byte) 0x10),

    AllLiquids((byte) (Water.value | Ocean.value | Magma.value | Slime.value));



    @Getter(onMethod = @__({@Override}))
    public final byte value;

    public static LiquidHeaderTypeFlag valueOf(final int value) {
        return Arrays.stream(values()).filter(flag -> flag.value == value).findFirst().orElseThrow();
    }

}
