package com.github.azeroth.defines;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LineOfSightChecks implements EnumFlag.FlagValue {
    VMAP(0x1), // check static floor layout data
    GOBJECT(0x2), // check dynamic game object data

    ALL(VMAP.value | GOBJECT.value);

    public final int value;
}
