package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public
enum TradeSlots {

    SLOTS_0(0),
    SLOTS_1(1),
    SLOTS_2(2),
    SLOTS_3(3),
    SLOTS_4(4),
    SLOTS_5(5),
    SLOTS_6(6),
    COUNT(7),
    INVALID(-1);

    public static final TradeSlots TRADED_COUNT = SLOTS_6;
    public static final TradeSlots NON_TRADED = SLOTS_6;

    public final int value;
}
