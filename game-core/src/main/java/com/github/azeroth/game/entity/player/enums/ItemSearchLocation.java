package com.github.azeroth.game.entity.player.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemSearchLocation implements EnumFlag.FlagValue {
    Equipment(0x01),
    Inventory(0x02),
    Bank(0x04),
    ReagentBank(0x08),

    Default(Equipment.value | Inventory.value),
    Everywhere(Equipment.value | Inventory.value | Bank.value | ReagentBank.value);
    public final int value;
}
