package com.github.azeroth.defines;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)

public enum MoneyUnit {
    COPPER(1), SILVER(COPPER.value * 100), GOLD(SILVER.value * 100);

    public final int value;

}
