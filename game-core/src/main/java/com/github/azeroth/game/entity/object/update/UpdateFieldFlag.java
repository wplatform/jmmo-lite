package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UpdateFieldFlag implements EnumFlag.FlagValue {

    None(0),
    Owner(0x01),
    PartyMember(0x02),
    UnitAll(0x04),
    Empath(0x08);

    public final int value;
}
