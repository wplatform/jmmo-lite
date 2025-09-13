package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public
enum InventoryPackSlot                              // 24 slots
{
    ITEM_START(23),
    ITEM_END(47);
    public final int index;
}
