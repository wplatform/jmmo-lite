package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public
enum ItemSearchLocation {
    Equipment(0x01),
    Inventory(0x02),
    Bank(0x04),
    ReagentBank(0x08),

    Default(Equipment.value | Inventory.value),
    Everywhere(Equipment.value | Inventory.value | Bank.value | ReagentBank.value);
    public final int value;
}
