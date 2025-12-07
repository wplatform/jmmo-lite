package com.github.azeroth.game.domain.unit;

public enum VictimState {
    INTACT, // set when attacker misses
    HIT, // victim got clear/blocked hit
    DODGE,
    PARRY,
    INTERRUPT,
    BLOCKS, // unused? not set when blocked, even on full block
    EVADES,
    IS_IMMUNE,
    DEFLECTS
}
