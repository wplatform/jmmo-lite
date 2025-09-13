package com.github.azeroth.game.map.grid;


import com.github.azeroth.common.Assert;
import com.github.azeroth.game.domain.map.Coordinate;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.map.Map;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.map.grid.visitor.GridVisitor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
public class Cell {


    public final int gridX;
    public final int gridY;
    public final int cellX;
    public final int cellY;
    public boolean noCreate;
    public Cell(Coordinate cellCoordinate) {
        if(cellCoordinate.isGridCoordinate()) {
            throw new IllegalArgumentException("Cell coordinate must be cell coordinate");
        }
        gridX = cellCoordinate.axisX() / MapDefine.MAX_NUMBER_OF_CELLS;
        gridY = cellCoordinate.axisY() / MapDefine.MAX_NUMBER_OF_CELLS;
        cellX = cellCoordinate.axisX() % MapDefine.MAX_NUMBER_OF_CELLS;
        cellY = cellCoordinate.axisY() % MapDefine.MAX_NUMBER_OF_CELLS;
    }

    public Cell(float x, float y) {
        Coordinate p = MapDefine.computeCellCoordinate(x, y);
        gridX = p.axisX() / MapDefine.MAX_NUMBER_OF_CELLS;
        gridY = p.axisY() / MapDefine.MAX_NUMBER_OF_CELLS;
        cellX = p.axisX() % MapDefine.MAX_NUMBER_OF_CELLS;
        cellY = p.axisY() % MapDefine.MAX_NUMBER_OF_CELLS;
    }

    public Cell(Cell cell) {
        gridX = cell.gridX;
        gridY = cell.gridY;
        cellX = cell.cellX;
        cellY = cell.cellY;
        noCreate = cell.noCreate;
    }

    public static Area calculateCellArea(float x, float y, float radius) {
        if (radius <= 0.0f) {
            Coordinate center = MapDefine.computeCellCoordinate(x, y).normalize();
            return new Area(center, center);
        }

        Coordinate centerX = MapDefine.computeCellCoordinate(x - radius, y - radius).normalize();
        Coordinate centerY = MapDefine.computeCellCoordinate(x + radius, y + radius).normalize();
        return new Area(centerX, centerY);
    }

    public static void visitGrid(WorldObject center, GridVisitor visitor, float radius) {
        visitGrid(center, visitor, radius, true);
    }

    public static void visitGrid(WorldObject center, GridVisitor visitor, float radius, boolean dontLoad) {

        var p = MapDefine.computeCellCoordinate(center.getLocation().getX(), center.getLocation().getY());
        Cell cell = new Cell(p);

        if (dontLoad) {
            cell.setNoCreate();
        }

        cell.visit(p, visitor, center.getMap(), center, radius);
    }

    public final boolean isCellValid() {
        return cellX < MapDefine.MAX_NUMBER_OF_CELLS && cellY < MapDefine.MAX_NUMBER_OF_CELLS;
    }

    public final int getGridId() {
        //Create a grid coordinate and call the coordinate.getId() is same as gridX * MapDefine.MAX_NUMBER_OF_GRIDS + gridY
        Assert.state(gridX < MapDefine.MAX_NUMBER_OF_GRIDS && gridY < MapDefine.MAX_NUMBER_OF_GRIDS, "x = {}, y = {}", gridX, gridY);
        return gridX * MapDefine.MAX_NUMBER_OF_GRIDS + gridY;
    }

    public final int getCellId() {
        return cellX * MapDefine.MAX_NUMBER_OF_CELLS + cellY;
    }

    public void setNoCreate() {
        noCreate = true;
    }

    public Coordinate getCellCoordinate() {
        return Coordinate.createCellCoordinate(gridX * MapDefine.MAX_NUMBER_OF_CELLS + cellX,
                gridY * MapDefine.MAX_NUMBER_OF_CELLS + cellY);
    }

    public boolean diffCell(Cell cell) {
        return (cellX != cell.cellX ||
                cellY != cell.cellY);
    }

    public boolean diffGrid(Cell cell) {
        return (gridX != cell.gridX ||
                gridY != cell.gridY);
    }

    public final void visit(Coordinate standing_cell, GridVisitor visitor, Map map, WorldObject object, float radius) {
        //we should increase search radius by object's radius, otherwise
        //we could have problems with huge creatures, which won't attack nearest players etc
        visit(standing_cell, visitor, map, object.getLocation().getX(), object.getLocation().getY(), radius + object.getCombatReach());
    }

    public final void visit(Coordinate standing_cell, GridVisitor visitor, Map map, float x_off, float y_off, float radius) {
        if (!standing_cell.isCoordinateValid())
            return;

        //no jokes here... Actually placing ASSERT() here was good idea, but
        //we had some problems with DynamicObjects, which pass radius = 0.0f (DB issue?)
        //maybe it is better to just return when radius <= 0.0f?
        if (radius <= 0.0f) {
            map.visit(this, visitor);
            return;
        }
        //lets limit the upper value for search radius
        if (radius > MapDefine.SIZE_OF_GRIDS)
            radius = MapDefine.SIZE_OF_GRIDS;

        //lets calculate object coord offsets from cell borders.
        Area area = calculateCellArea(x_off, y_off, radius);
        //if radius fits inside standing cell
        if (area.isSingleCellArea()) {
            map.visit(this, visitor);
            return;
        }

        //visit all cells, found in CalculateCellArea()
        //if radius is known to reach cell area more than 4x4 then we should call optimized VisitCircle
        //currently this technique works with MAX_NUMBER_OF_CELLS 16 and higher, with lower values
        //there are nothing to optimize because SIZE_OF_GRID_CELL is too big...
        if ((area.getHighBound().axisX() > (area.getLowBound().axisX() + 4)) && (area.getHighBound().axisY() > (area.getLowBound().axisY() + 4))) {
            visitCircle(visitor, map, area.getLowBound(), area.getHighBound());
            return;
        }

        //ALWAYS visit standing cell first!!! Since we deal with small radiuses
        //it is very essential to call visitor for standing cell firstly...
        map.visit(this, visitor);

        // loop the cell range
        for (int x = area.getLowBound().axisX(); x <= area.getHighBound().axisX(); ++x) {
            for (int y = area.getLowBound().axisY(); y <= area.getHighBound().axisY(); ++y) {
                Coordinate cellCoord = Coordinate.createCellCoordinate(x, y);
                //lets skip standing cell since we already visited it
                if (!Objects.equals(cellCoord, standing_cell)) {
                    Cell r_zone = new Cell(cellCoord);
                    r_zone.noCreate = this.noCreate;
                    map.visit(r_zone, visitor);
                }
            }
        }
    }

    public final void visitCircle(GridVisitor visitor, Map map, Coordinate begin_cell, Coordinate end_cell) {
        //here is an algorithm for 'filling' circum-squared octagon
        int x_shift = (int) Math.ceil((end_cell.axisX() - begin_cell.axisX()) * 0.3f - 0.5f);
        //lets calculate x_start/x_end coords for central strip...
        final int x_start = begin_cell.axisX() + x_shift;
        final int x_end = end_cell.axisX() - x_shift;

        //visit central strip with constant width...
        for (int x = x_start; x <= x_end; ++x) {
            for (int y = begin_cell.axisY(); y <= end_cell.axisY(); ++y) {
                Coordinate cellCoord = Coordinate.createCellCoordinate(x, y);
                Cell r_zone = new Cell(cellCoord);
                r_zone.noCreate = this.noCreate;
                map.visit(r_zone, visitor);
            }
        }

        //if x_shift == 0 then we have too small cell area, which were already
        //visited at previous step, so just return from procedure...
        if (x_shift == 0)
            return;

        int y_start = end_cell.axisY();
        int y_end = begin_cell.axisY();
        //now we are visiting borders of an octagon...
        for (int step = 1; step <= (x_start - begin_cell.axisX()); ++step) {
            //each step reduces strip height by 2 cells...
            y_end += 1;
            y_start -= 1;
            for (int y = y_start; y >= y_end; --y) {
                //we visit cells symmetrically from both sides, heading from center to sides and from up to bottom
                //e.g. filling 2 trapezoids after filling central cell strip...
                Coordinate cellCoord_left = Coordinate.createCellCoordinate(x_start - step, y);
                Cell r_zone_left = new Cell(cellCoord_left);
                r_zone_left.noCreate = this.noCreate;
                map.visit(r_zone_left, visitor);

                //right trapezoid cell visit
                Coordinate cellCoord_right = Coordinate.createCellCoordinate(x_end + step, y);
                Cell r_zone_right = new Cell(cellCoord_right);
                r_zone_right.noCreate = this.noCreate;
                map.visit(r_zone_right, visitor);
            }
        }
    }

    public final void visit(WorldObject center, GridVisitor visitor, float radius, boolean dontLoad) {
        Coordinate coordinate = MapDefine.computeCellCoordinate(center.getLocation().getX(), center.getLocation().getY());
        Cell cell = new Cell(coordinate);
        if (dontLoad)
            cell.setNoCreate();
        cell.visit(coordinate, visitor, center.getMap(), center, radius);
    }

    public final void visit(float x, float y, Map map, GridVisitor visitor, float radius, boolean dontLoad) {
        Coordinate coordinate = MapDefine.computeCellCoordinate(x, y);
        Cell cell = new Cell(coordinate);
        if (dontLoad)
            cell.setNoCreate();
        cell.visit(coordinate, visitor, map, x, y, radius);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Area {
        private Coordinate lowBound;
        private Coordinate highBound;


        public boolean isSingleCellArea() {
            return lowBound.equals(highBound);
        }

    }

}

