package com.github.azeroth.game.map.grid;

import com.github.azeroth.game.domain.map.Coordinate;
import com.github.azeroth.game.map.grid.visitor.GridVisitor;
import lombok.Getter;
import lombok.Setter;

import static com.github.azeroth.game.domain.map.MapDefine.MAX_NUMBER_OF_CELLS;

@Getter
@Setter
public class NGrid {

    private final int x;
    private final int y;
    private final NCell[][] cells = new NCell[MAX_NUMBER_OF_CELLS][MAX_NUMBER_OF_CELLS];

    private final int gridId;
    private int unloadActiveLockCount;
    private boolean unloadExplicitLock;
    private volatile int refCnt = 0;


    public NGrid(int id, int x, int y) {
        gridId = id;
        this.x = x;
        this.y = y;

    }


    public NGrid(Cell cell) {
        Coordinate coordinate = Coordinate.createGridCoordinate(cell.getGridX(), cell.getGridY());

        this(cell.getGridId(), cell.getGridX(), cell.getGridY());
    }

    public final NCell getNCell(int x, int y) {
        return cells[x][y];
    }


    public final void visitGrid(GridVisitor visitor) {
        for (int x = 0; x < cells.length; ++x) {
            for (int y = 0; y < cells[x].length; ++y) {
                getNCell(x, y).visit(visitor);
            }
        }
    }


    public final void visitGrid(int x, int y, GridVisitor visitor) {
        getNCell(x, y).visit(visitor);
    }

    public final int getWorldObjectCountInNGrid() {
        int count = 0;
        for (int x = 0; x < cells.length; ++x) {
            for (int y = 0; y < cells[x].length; ++y) {
                count += getNCell(x, y).getWorldObjectCountInCell();
            }
        }
        return count;
    }

}
