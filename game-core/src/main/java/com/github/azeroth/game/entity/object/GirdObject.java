package com.github.azeroth.game.entity.object;

import com.github.azeroth.game.map.grid.Cell;


public interface GirdObject {
    default boolean isInGrid() {
        return getCurrentCell() != null;
    }

    void setNewCellPosition(float x, float y, float z, float o);

    Cell getCurrentCell();

    void setCurrentCell(Cell cell);

}
