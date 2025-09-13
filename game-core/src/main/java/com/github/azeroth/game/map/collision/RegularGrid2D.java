package com.github.azeroth.game.map.collision;


import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.github.azeroth.common.Functions;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.domain.map.Distance;
import com.github.azeroth.game.map.model.GameObjectModel;
import com.github.azeroth.game.map.model.LocationInfo;
import com.github.azeroth.game.domain.map.enums.ModelIgnoreFlags;
import com.github.azeroth.game.domain.map.AreaInfo;
import com.github.azeroth.game.domain.phasing.PhaseShift;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class RegularGrid2D {
    private static final int CELL_NUMBER = MapDefine.MAX_NUMBER_OF_GRIDS;
    private static final float CELL_SIZE = MapDefine.SIZE_OF_GRIDS;

    private final HashMap<GameObjectModel, List<Node>> memberTable = new HashMap<>();
    private final Node[][] nodes = new Node[CELL_NUMBER][CELL_NUMBER];


    public void insert(GameObjectModel value) {
        synchronized (memberTable) {
            var bounds = value.getBoundingBox();
            var low = Cell.computeCell(bounds.min.x, bounds.min.y);
            var high = Cell.computeCell(bounds.max.x, bounds.max.y);

            for (var x = low.x; x <= high.x; ++x) {
                for (var y = low.y; y <= high.y; ++y) {
                    var node = getGrid(x, y);
                    node.insert(value);
                    memberTable.compute(value, Functions.addToList(node));
                }
            }
        }
    }

    public void remove(GameObjectModel value) {
        // Remove the member

        synchronized (memberTable) {
            memberTable.remove(value);
        }
    }

    public void balance() {
        for (var x = 0; x < CELL_NUMBER; ++x) {
            for (var y = 0; y < CELL_NUMBER; ++y) {
                var n = nodes[x][y];

                if (n != null) {
                    n.balance();
                }
            }
        }
    }

    public final boolean contains(GameObjectModel value) {
        synchronized (memberTable) {
            return memberTable.containsKey(value);
        }
    }

    public final boolean empty() {
        synchronized (memberTable) {
            return memberTable.isEmpty();
        }
    }

    public final void intersectRay(Ray ray, Vector3 end, PhaseShift phaseShift, Distance max_dist) {
        var cell = Cell.computeCell(ray.origin.x, ray.origin.x);

        if (!cell.isValid()) {
            return;
        }

        var last_cell = Cell.computeCell(end.x, end.y);

        if (Objects.equals(cell, last_cell)) {
            var node = nodes[cell.x][cell.y];

            if (node != null) {
                node.intersectRay(ray, phaseShift, max_dist);
            }

            return;
        }

        var voxel = CELL_SIZE;


        Vector3 invDirection = new Vector3(1f / ray.direction.x, 1f / ray.direction.y, 1f / ray.direction.z);

        float kx_inv = invDirection.x, bx = ray.origin.x;
        float ky_inv = invDirection.y, by = ray.origin.y;

        int stepX, stepY;
        float tMaxX, tMaxY;

        if (kx_inv >= 0) {
            stepX = 1;
            var x_border = (cell.x + 1) * voxel;
            tMaxX = (x_border - bx) * kx_inv;
        } else {
            stepX = -1;
            var x_border = (cell.x - 1) * voxel;
            tMaxX = (x_border - bx) * kx_inv;
        }

        if (ky_inv >= 0) {
            stepY = 1;
            var y_border = (cell.y + 1) * voxel;
            tMaxY = (y_border - by) * ky_inv;
        } else {
            stepY = -1;
            var y_border = (cell.y - 1) * voxel;
            tMaxY = (y_border - by) * ky_inv;
        }

        var tDeltaX = voxel * Math.abs(kx_inv);
        var tDeltaY = voxel * Math.abs(ky_inv);

        do {
            var node = nodes[cell.x][cell.y];

            if (node != null) {
                node.intersectRay(ray, phaseShift, max_dist);
            }

            if (Objects.equals(cell, last_cell)) {
                break;
            }

            if (tMaxX < tMaxY) {
                tMaxX += tDeltaX;
                cell.x += stepX;
            } else {
                tMaxY += tDeltaY;
                cell.y += stepY;
            }
        } while (cell.isValid());
    }

    public final void intersectPoint(Vector3 point, PhaseShift phaseShift, AreaInfo areaInfo) {
        var cell = Cell.computeCell(point.x, point.y);

        if (!cell.isValid()) {
            return;
        }

        var node = nodes[cell.x][cell.y];

        if (node != null) {
            node.intersectPoint(point, phaseShift, areaInfo);
        }
    }

    public final void intersectPoint(Vector3 point, PhaseShift phaseShift, LocationInfo locationInfo) {
        var cell = Cell.computeCell(point.x, point.y);

        if (!cell.isValid()) {
            return;
        }

        var node = nodes[cell.x][cell.y];

        if (node != null) {
            node.intersectPoint(point, phaseShift, locationInfo);
        }
    }

    // Optimized verson of intersectRay function for rays with vertical directions
    public final void intersectZAllignedRay(Ray ray, PhaseShift phaseShift, Distance max_dist) {
        var cell = Cell.computeCell(ray.origin.x, ray.origin.y);

        if (!cell.isValid()) {
            return;
        }

        var node = nodes[cell.x][cell.y];

        if (node != null) {
            node.intersectRay(ray, phaseShift, max_dist);
        }
    }

    private Node getGrid(int x, int y) {
        if (nodes[x][y] == null) {
            nodes[x][y] = new Node();
        }

        return nodes[x][y];
    }


    public static class Node {
        private final BIH tree = new BIH();
        private final ArrayList<GameObjectModel> objects = new ArrayList<>();
        private int unbalancedTimes;

        public final void insert(GameObjectModel obj) {
            ++unbalancedTimes;
            objects.add(obj);
        }

        public final void remove(GameObjectModel obj) {
            ++unbalancedTimes;
            objects.remove(obj);
        }

        public final void balance() {
            if (unbalancedTimes == 0) {
                return;
            }

            synchronized (objects) {
                unbalancedTimes = 0;
                tree.build(objects);
            }
        }

        public final void intersectRay(Ray ray, PhaseShift phaseShift, Distance max_dist) {
            balance();
            tree.intersectRay(ray, max_dist, true, (r, entry, dist, stopAtFirst) -> {
                if (entry >= objects.size()) {
                    return false;
                }

                var obj = objects.get(entry);

                if (obj != null) {
                    return obj.intersectRay(r, dist, stopAtFirst, phaseShift, ModelIgnoreFlags.Nothing);
                }
                return false;
            });

        }

        public final void intersectPoint(Vector3 point, PhaseShift phaseShift, AreaInfo areaInfo) {
            balance();
            tree.intersectPoint(point, (vector3, entry) -> {
                if (entry >= objects.size()) {
                    return;
                }
                var obj = objects.get(entry);
                if (obj != null) {
                    obj.intersectPoint(vector3, areaInfo, phaseShift);
                }
            });
        }

        public final void intersectPoint(Vector3 point, PhaseShift phaseShift, LocationInfo locationInfo) {
            balance();
            tree.intersectPoint(point, (vector3, entry) -> {
                if (entry >= objects.size()) {
                    return;
                }
                var obj = objects.get(entry);
                if (obj != null) {
                    obj.getLocationInfo(vector3, locationInfo, phaseShift);
                }
            });
        }

    }

    @EqualsAndHashCode
    public final static class Cell {
        public int x, y;

        public static Cell computeCell(float fx, float fy) {
            Cell c = new Cell();
            c.x = (int) (fx * (1.0f / CELL_SIZE) + (CELL_NUMBER / 2f));
            c.y = (int) (fy * (1.0f / CELL_SIZE) + (CELL_NUMBER / 2f));

            return c;
        }

        public boolean isValid() {
            return x >= 0 && x < CELL_NUMBER && y >= 0 && y < CELL_NUMBER;
        }

    }

}
