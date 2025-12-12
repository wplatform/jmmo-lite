package com.github.azeroth.game.domain.map;

import com.badlogic.gdx.math.Matrix3;
import com.github.azeroth.game.domain.object.Position;

import java.time.Duration;

public interface MapDefine {

    int MAP_MAGIC = 0x5350414D; //"MAPS";
    int MAP_VERSION_MAGIC = 10;
    int MAP_VERSION_MAGIC2 = 0x302E3276; //"v2.0"; // Hack for some different extractors using v2.0 header
    int MAP_AREA_MAGIC = 0x41455241;     //"AREA";
    int MAP_HEIGHT_MAGIC = 0x5447484D;   //"MHGT";
    int MAP_LIQUID_MAGIC = 0x51494C4D;   //"MLIQ";

    float LIQUID_TILE_SIZE = (533.333f / 128.f);


    String VMAP_MAGIC = "VMAP_4.E";
    // used in extracted vmap files with raw data
    String GAME_OBJECT_MODELS = "GameObjectModels.dtree";
    String RAW_VMAP_MAGIC = "VMAP04E";

    int MMAP_MAGIC = 0x4d4d4150; // 'MMAP'
    int MMAP_VERSION = 16;


    float VMAP_INVALID_HEIGHT = -100000.0f; // for check
    // real assigned value in unknown height case
    float DEFAULT_COLLISION_HEIGHT = 2.03128f;
    float VMAP_INVALID_HEIGHT_VALUE = -200000.0f;
    int MAP_INVALID_ZONE = 0xFFFFFFFF;

    int MAX_NUMBER_OF_GRIDS = 64;

    int MAX_NUMBER_OF_CELLS = 8;

    float SIZE_OF_GRIDS = 533.3333f;

    int CENTER_GRID_ID = (MAX_NUMBER_OF_GRIDS / 2);

    float CENTER_GRID_OFFSET = (SIZE_OF_GRIDS / 2);

    long MIN_GRID_DELAY = Duration.ofMinutes(1).toMillis();

    int MIN_MAP_UPDATE_DELAY = 1;

    float SIZE_OF_GRID_CELL = (SIZE_OF_GRIDS / MAX_NUMBER_OF_CELLS);
    float CENTER_GRID_CELL_OFFSET = (SIZE_OF_GRID_CELL / 2);
    int CENTER_GRID_CELL_ID = (MAX_NUMBER_OF_CELLS * MAX_NUMBER_OF_GRIDS) / 2;
    int TOTAL_NUMBER_OF_CELLS_PER_MAP = (MAX_NUMBER_OF_GRIDS * MAX_NUMBER_OF_CELLS);

    int MAP_RESOLUTION = 128;

    float MAP_SIZE = (SIZE_OF_GRIDS * MAX_NUMBER_OF_GRIDS);

    float MAP_HALF_SIZE = (MAP_SIZE / 2);

    float MAX_HEIGHT = 100000.0f;                     // can be use for find ground height at surface
    float INVALID_HEIGHT = -100000.0f;                // for check, must be equal to VMAP_INVALID_HEIGHT, real second for unknown height is VMAP_INVALID_HEIGHT_VALUE
    float MAX_FALL_DISTANCE = 250000.0f;              // "unlimited fall" to find VMap ground if it is available, just larger than MAX_HEIGHT - INVALID_HEIGHT
    float DEFAULT_HEIGHT_SEARCH = 50.0f;              // default search distance to find height at nearby locations

    int DEFAULT_VISIBILITY_NOTIFY_PERIOD = 1000;
    int SPAWN_GROUP_MAP_UNSET = 0xFFFFFFFF;


    static boolean isValidMapCoordinate(float c) {
        return Float.isFinite(c) && Math.abs(c) <= MAP_HALF_SIZE - 0.5f;
    }

    static boolean isValidMapCoordinate(float x, float y) {
        return isValidMapCoordinate(x) && isValidMapCoordinate(y);
    }

    static boolean isValidMapCoordinate(Position pos) {
        return isValidMapCoordinate(pos.getX(), pos.getY()) && isValidMapCoordinate(pos.getZ());
    }

    static boolean isValidMapCoordinate(float x, float y, float z) {
        return isValidMapCoordinate(x, y) && isValidMapCoordinate(z);
    }

    static boolean isValidMapCoordinate(float x, float y, float z, float o) {
        return isValidMapCoordinate(x, y, z) && Float.isFinite(o);
    }


    static float normalizeMapCoordinate(float c) {
        if (c > MAP_HALF_SIZE - 0.5f)
            c = MAP_HALF_SIZE - 0.5f;
        else if (c < -(MAP_HALF_SIZE - 0.5f))
            c = -(MAP_HALF_SIZE - 0.5f);

        return c;
    }

    static Coordinate computeGridCoordinate(float x, float y) {
        var x_offset = (x - CENTER_GRID_OFFSET) / SIZE_OF_GRIDS;
        var y_offset = (y - CENTER_GRID_OFFSET) / SIZE_OF_GRIDS;

        var x_val = (int) (x_offset + CENTER_GRID_ID + 0.5f);
        var y_val = (int) (y_offset + CENTER_GRID_ID + 0.5f);

        return Coordinate.createGridCoordinate(x_val, y_val);
    }

    static Coordinate computeGridCoordinateSimple(float x, float y) {
        var gx = (int) (CENTER_GRID_ID - x / SIZE_OF_GRIDS);
        var gy = (int) (CENTER_GRID_ID - y / SIZE_OF_GRIDS);
        return Coordinate.createGridCoordinate((MAX_NUMBER_OF_GRIDS - 1) - gx, (MAX_NUMBER_OF_GRIDS - 1) - gy);
    }

    static Coordinate computeCellCoordinate(float x, float y) {
        var x_offset = (x - CENTER_GRID_CELL_OFFSET) / SIZE_OF_GRID_CELL;
        var y_offset = (y - CENTER_GRID_CELL_OFFSET) / SIZE_OF_GRID_CELL;
        var x_val = (int) (x_offset + CENTER_GRID_CELL_ID + 0.5f);
        var y_val = (int) (y_offset + CENTER_GRID_CELL_ID + 0.5f);
        return Coordinate.createCellCoordinate(x_val, y_val);
    }

    static Matrix3 fromEulerAnglesZYX(float fYAngle, float fPAngle, float fRAngle) {
        float fCos, fSin;

        fCos = (float) Math.cos(fYAngle);
        fSin = (float) Math.sin(fYAngle);
        Matrix3 kZMat = new Matrix3(new float[]{fCos, -fSin, 0.0f, fSin, fCos, 0.0f, 0.0f, 0.0f, 1.0f});

        fCos = (float) Math.cos(fPAngle);
        fSin = (float) Math.sin(fPAngle);
        Matrix3 kYMat = new Matrix3(new float[]{fCos, 0.0f, fSin, 0.0f, 1.0f, 0.0f, -fSin, 0.0f, fCos});

        fCos = (float) Math.cos(fRAngle);
        fSin = (float) Math.sin(fRAngle);
        Matrix3 kXMat = new Matrix3(new float[]{1.0f, 0.0f, 0.0f, 0.0f, fCos, -fSin, 0.0f, fSin, fCos});

        return kZMat.mul((kYMat.mul(kXMat)));
    }


}
