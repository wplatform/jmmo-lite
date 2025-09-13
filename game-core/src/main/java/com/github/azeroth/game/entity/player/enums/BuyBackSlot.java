package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public
enum BuyBackSlot                                           // 12 slots
{
    // stored in m_buybackitems
    START(82),
    END(94);
    public final int index;
}
