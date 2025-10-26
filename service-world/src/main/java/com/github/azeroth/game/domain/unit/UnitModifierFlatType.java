package com.github.azeroth.game.domain.unit;

public enum UnitModifierFlatType {
    BASE_VALUE,
    BASE_PCT_EXCLUDE_CREATE,    // percent modifier affecting all stat values from auras and gear but not player base for level
    TOTAL_VALUE
}
