package com.github.azeroth.game.movement.enums;

public interface NavArea {
    byte EMPTY = 0;
    // areas 1-60 will be used for destructible areas (currently skipped in vmaps, WMO with flag 1)
    // ground is the highest value to make recast choose ground over water when merging surfaces very close to each other (shallow water would be walkable)
    byte MAGMA_SLIME = 8; // don't need to differentiate between them
    byte WATER = 9;
    byte GROUND_STEEP = 10;
    byte GROUND = 11;
    byte MAX_VALUE = 11;
    byte MIN_VALUE = 8;
    byte ALL_MASK = 0x3F; // max allowed second
}
