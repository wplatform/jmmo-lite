package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public
enum InventorySlot                                 // 4 slots
{
    BAG_START(19),
    BAG_END(23);
    public final int index;
}
