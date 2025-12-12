package com.github.azeroth.game.movement.enums;


public interface NavTerrainFlag {
    int EMPTY = 0x00;
    int GROUND = 1 << (NavArea.MAX_VALUE - NavArea.GROUND);
    int GROUND_STEEP = 1 << (NavArea.MAX_VALUE - NavArea.GROUND_STEEP);
    int WATER = 1 << (NavArea.MAX_VALUE - NavArea.WATER);
    int MAGMA_SLIME = 1 << (NavArea.MAX_VALUE - NavArea.MAGMA_SLIME);
}
