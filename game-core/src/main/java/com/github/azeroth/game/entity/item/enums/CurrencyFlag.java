package com.github.azeroth.game.entity.item.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public
enum CurrencyFlag {
    TRADEABLE(0x01),
    // ...
    HIGH_PRECISION(0x08),
    // ...
    COUNT_SEASON_TOTAL(0x80);

    public final int value;
}
