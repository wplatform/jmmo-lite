package com.github.azeroth.game.map.grid;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.common.Logs;
import com.github.azeroth.dbc.DbcObjectManager;
import com.github.azeroth.dbc.domain.AreaTable;
import com.github.azeroth.dbc.domain.LiquidType;
import com.github.azeroth.game.domain.map.*;
import com.github.azeroth.game.domain.map.enums.LiquidHeaderTypeFlag;
import com.github.azeroth.game.domain.map.enums.LoadResult;
import com.github.azeroth.game.domain.map.enums.ZLiquidStatus;
import com.github.azeroth.utils.Flags;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.BiFunction;

import static com.github.azeroth.defines.SharedDefine.GROUND_HEIGHT_TOLERANCE;
import static com.github.azeroth.game.domain.map.MapDefine.*;

public class GridMap {


    // due to floating point precision issues, we have to resort to a small hack to fix inconsistencies in liquids
    private static final float GROUND_LEVEL_OFFSET_HACK = 0.02f;


    private static final int[][] INDICES = {
            {3, 0, 4},
            {0, 1, 4},
            {1, 2, 4},
            {2, 5, 4},
            {5, 8, 4},
            {8, 7, 4},
            {7, 6, 4},
            {6, 3, 4}
    };

    private static final float[][] BOUND_GRID_COORDINATE = {
            {0.0f, 0.0f},
            {0.0f, -266.66666f},
            {0.0f, -533.33331f},
            {-266.66666f, 0.0f},
            {-266.66666f, -266.66666f},
            {-266.66666f, -533.33331f},
            {-533.33331f, 0.0f},
            {-533.33331f, -266.66666f},
            {-533.33331f, -533.33331f}
    };
    private final DbcObjectManager dbcObjectManager;
    public float[] V9;
    public int[] uint16V9;
    public short[] ubyteV9;
    public float[] V8;
    public int[] uint16V8;
    public short[] ubyteV8;
    //Area data
    public int[] areaMap;
    private BiFunction<Float, Float, Float> gridGetHeightFunc;
    private Plane[] _minHeightPlanes;
    private float gridHeight;
    private float gridIntHeightMultiplier;
    //Liquid Map
    private float liquidLevel;
    private short[] liquidEntry;
    private byte[] liquidFlags;
    private float[] liquidMap;
    private short _gridArea;
    private short liquidGlobalEntry;
    private LiquidHeaderTypeFlag liquidGlobalFlags;
    private byte liquidOffX;
    private byte liquidOffY;
    private byte liquidWidth;
    private byte liquidHeight;
    private byte[] holes;


    public GridMap(DbcObjectManager dbcObjectManager) {
        // Height level data
        gridHeight = MapDefine.INVALID_HEIGHT;
        gridGetHeightFunc = this::getHeightFromFlat;
        liquidGlobalFlags = LiquidHeaderTypeFlag.NoWater;
        // Liquid data
        liquidLevel = MapDefine.INVALID_HEIGHT;
        this.dbcObjectManager = dbcObjectManager;
    }

    public LoadResult loadData(Path filename) {
        // Unload old data if exist
        unloadData();

        // Not return error if file not found
        if (!Files.exists(filename))
            return LoadResult.FileNotFound;

        try (FileChannel fileChannel = FileChannel.open(filename, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(MapFileHeader.BYTES);
            fileChannel.read(buffer);
            buffer.flip();
            MapFileHeader header = new MapFileHeader(buffer);
            if (header.mapMagic() == MAP_MAGIC && header.versionMagic() == MAP_VERSION_MAGIC) {
                // load up area data
                if (header.areaMapOffset() != 0 && !loadAreaData(fileChannel, header.areaMapOffset())) {
                    Logs.MAPS.error("Error loading map area data");
                    return LoadResult.ReadFromFileFailed;
                }
                // load up height data
                if (header.heightMapOffset() != 0 && !loadHeightData(fileChannel, header.heightMapOffset())) {
                    Logs.MAPS.error("Error loading map height data");
                    return LoadResult.ReadFromFileFailed;
                }
                // load up liquid data
                if (header.liquidMapOffset() != 0 && !loadLiquidData(fileChannel, header.liquidMapOffset())) {
                    Logs.MAPS.error("Error loading map liquids data");
                    return LoadResult.ReadFromFileFailed;
                }
                // loadup holes data (if any. check header.holesOffset)
                if (header.holesSize() != 0 && !loadHolesData(fileChannel, header.holesOffset())) {
                    Logs.MAPS.error("Error loading map holes data");
                    return LoadResult.ReadFromFileFailed;
                }
                return LoadResult.Success;
            }

        } catch (IOException e) {
            Logs.MAPS.error("Error loading map file {} error.", filename.toAbsolutePath(), e);
        }
        return LoadResult.ReadFromFileFailed;
    }

    public void unloadData() {
        areaMap = null;
        V9 = null;
        V8 = null;
        liquidEntry = null;
        liquidFlags = null;
        liquidMap = null;
        gridGetHeightFunc = this::getHeightFromFlat;
    }

    public int getArea(float x, float y) {
        if (areaMap == null)
            return _gridArea;

        x = 16 * (CENTER_GRID_ID - x / SIZE_OF_GRIDS);
        y = 16 * (CENTER_GRID_ID - y / SIZE_OF_GRIDS);
        int lx = (int) x & 15;
        int ly = (int) y & 15;

        return areaMap[lx * 16 + ly];
    }

    public float GetMinHeight(float x, float y) {
        if (_minHeightPlanes == null)
            return -500.0f;

        Coordinate gridCoordinate = MapDefine.computeGridCoordinateSimple(x, y);

        int doubleGridX = (int) Math.floor(-(x - MAP_HALF_SIZE) / CENTER_GRID_OFFSET);
        int doubleGridY = (int) Math.floor(-(y - MAP_HALF_SIZE) / CENTER_GRID_OFFSET);

        float gx = x - (gridCoordinate.axisX() - CENTER_GRID_ID + 1) * SIZE_OF_GRIDS;
        float gy = y - (gridCoordinate.axisY() - CENTER_GRID_ID + 1) * SIZE_OF_GRIDS;

        int quarterIndex;
        if ((doubleGridY & 1) != 0) {
            if ((doubleGridX & 1) != 0)
                quarterIndex = 4 + (gx <= gy ? 1 : 0);
            else
                quarterIndex = 2 + (-SIZE_OF_GRIDS - gx > gy ? 1 : 0);
        } else if ((doubleGridX & 1) != 0) {
            quarterIndex = 6 + (-SIZE_OF_GRIDS - gx <= gy ? 1 : 0);
        } else {
            quarterIndex = gx > gy ? 1 : 0;
        }

        Ray ray = new Ray(new Vector3(gx, gy, 0.0f), Vector3.Z);
        Vector3 intersection = new Vector3();

        if (Intersector.intersectRayPlane(ray, _minHeightPlanes[quarterIndex], intersection)) {
            return intersection.z;
        }
        throw new IllegalStateException();

    }

    public float getLiquidLevel(float x, float y) {
        if (liquidMap == null)
            return liquidLevel;

        x = MAP_RESOLUTION * (CENTER_GRID_ID - x / SIZE_OF_GRIDS);
        y = MAP_RESOLUTION * (CENTER_GRID_ID - y / SIZE_OF_GRIDS);

        int cx_int = ((int) x & (MAP_RESOLUTION - 1)) - liquidOffY;
        int cy_int = ((int) y & (MAP_RESOLUTION - 1)) - liquidOffX;

        if (cx_int < 0 || cx_int >= liquidHeight)
            return INVALID_HEIGHT;
        if (cy_int < 0 || cy_int >= liquidWidth)
            return INVALID_HEIGHT;

        return liquidMap[cx_int * liquidWidth + cy_int];
    }

    // Get water state on map
    public EnumFlag<ZLiquidStatus> getLiquidStatus(float x, float y, float z, Byte reqLiquidType, LiquidData data, float collisionHeight) {
        // Check water type (if no water return)
        if (liquidGlobalFlags == LiquidHeaderTypeFlag.NoWater && liquidFlags != null)
            return EnumFlag.of(ZLiquidStatus.NO_WATER);

        // Get cell
        float cx = MAP_RESOLUTION * (CENTER_GRID_ID - x / SIZE_OF_GRIDS);
        float cy = MAP_RESOLUTION * (CENTER_GRID_ID - y / SIZE_OF_GRIDS);

        int x_int = (int) cx & (MAP_RESOLUTION - 1);
        int y_int = (int) cy & (MAP_RESOLUTION - 1);

        // Check water type in cell
        int idx = (x_int >> 3) * 16 + (y_int >> 3);

        LiquidHeaderTypeFlag type = liquidFlags != null ? LiquidHeaderTypeFlag.valueOf(liquidFlags[idx]) : liquidGlobalFlags;
        EnumFlag<LiquidHeaderTypeFlag> typeFlags = EnumFlag.of(type);
        int entry = liquidEntry != null ? liquidEntry[idx] : liquidGlobalEntry;
        LiquidType liquidType = dbcObjectManager.liquidType(entry);
        if (liquidType != null) {
            typeFlags.set(LiquidHeaderTypeFlag.DarkWater);
            int liqTypeIdx = liquidType.getSoundBank();
            if (entry < 21) {
                AreaTable areaTable = dbcObjectManager.areaTable(getArea(x, y));
                if (areaTable != null) {
                    Short overrideLiquid = areaTable.getLiquidTypeID(liqTypeIdx);
                    if (overrideLiquid != 0 && areaTable.getParentAreaID() != 0) {
                        areaTable = dbcObjectManager.areaTable(areaTable.getParentAreaID().intValue());

                        if (areaTable != null)
                            overrideLiquid = areaTable.getLiquidTypeID(liquidOffX);
                    }
                    LiquidType liq = dbcObjectManager.liquidType(overrideLiquid);
                    if (liq != null) {
                        entry = overrideLiquid;
                        liqTypeIdx = liq.getSoundBank();
                    }
                }
            }

            typeFlags.addFlag(LiquidHeaderTypeFlag.valueOf(1 << liqTypeIdx));
        }

        if (typeFlags.equals(LiquidHeaderTypeFlag.NoWater))
            return EnumFlag.of(ZLiquidStatus.NO_WATER);

        // Check req liquid type mask
        if (reqLiquidType != null && (reqLiquidType & type.getValue()) == LiquidHeaderTypeFlag.NoWater.getValue())
            return EnumFlag.of(ZLiquidStatus.NO_WATER);


        // Check water level:
        // Check water height map
        int lx_int = x_int - liquidOffY;
        int ly_int = y_int - liquidOffX;
        if (lx_int < 0 || lx_int >= liquidHeight)
            return EnumFlag.of(ZLiquidStatus.NO_WATER);
        if (ly_int < 0 || ly_int >= liquidWidth)
            return EnumFlag.of(ZLiquidStatus.NO_WATER);

        // Get water level
        float liquid_level = liquidMap != null ? liquidMap[lx_int * liquidWidth + ly_int] : liquidLevel;
        // Get ground level (sub 0.02 for fix some errors)
        float ground_level = getHeight(x, y);

        // Check water level and ground level
        if (liquid_level < (ground_level - GROUND_LEVEL_OFFSET_HACK) || z < (ground_level - GROUND_LEVEL_OFFSET_HACK))
            return EnumFlag.of(ZLiquidStatus.NO_WATER);

        // All ok in water -> store data
        if (data != null) {
            data.setEntry(entry);
            data.setTypeFlags(typeFlags);
            data.setLevel(liquid_level);
            data.setDepthLevel(ground_level);
        }

        // For speed check as int values
        float delta = liquid_level - z;

        // Above water
        EnumFlag<ZLiquidStatus> status = EnumFlag.of(ZLiquidStatus.ABOVE_WATER);

        if (delta > collisionHeight)            // Under water
            status.set(ZLiquidStatus.UNDER_WATER);
        else if (delta > 0.0f)                  // In water
            status.set(ZLiquidStatus.IN_WATER);
        else if (delta > -0.1f)                 // Walk on water
            status.set(ZLiquidStatus.WATER_WALK);

        if (!status.equals(ZLiquidStatus.ABOVE_WATER))
            if (Math.abs(ground_level - z) <= GROUND_HEIGHT_TOLERANCE)
                status.addFlag(ZLiquidStatus.OCEAN_FLOOR);

        return status;
    }

    public float getHeight(float x, float y) {
        return gridGetHeightFunc.apply(x, y);
    }

    private boolean loadAreaData(FileChannel channel, int offset) throws IOException {
        channel.position(offset);
        ByteBuffer buffer = ByteBuffer.allocate(6);
        channel.read(buffer);
        buffer.flip();
        MapAreaHeader areaHeader = new MapAreaHeader(buffer.getShort(), buffer.getShort(), buffer.getShort());

        if (areaHeader.fourcc() != MAP_AREA_MAGIC)
            return false;

        _gridArea = areaHeader.gridArea();

        if ((areaHeader.flags() & MapAreaHeader.FLAG_NO_AREA) != 0) {
            buffer = ByteBuffer.allocate(16 * 16 * 2);
            channel.read(buffer);
            areaMap = uint16ArrayToInt32Array(buffer.asShortBuffer().array());
        }
        return true;
    }

    private boolean loadHeightData(FileChannel channel, int offset) throws IOException {
        channel.position(offset);
        ByteBuffer buffer = ByteBuffer.allocate(MapHeightHeader.BYTES);
        channel.read(buffer);
        buffer.flip();

        var mapHeightHeader = new MapHeightHeader(buffer);
        if (mapHeightHeader.fourcc() != MAP_HEIGHT_MAGIC)
            return false;

        gridHeight = mapHeightHeader.gridHeight();

        if (!Flags.hasFlag(mapHeightHeader.flags(), MapHeightHeader.NoHeight)) {
            if (Flags.hasFlag(mapHeightHeader.flags(), MapHeightHeader.HeightAsInt16)) {

                buffer = ByteBuffer.allocate(129 * 129 * Short.BYTES);
                channel.read(buffer);
                uint16V9 = uint16ArrayToInt32Array(buffer.asShortBuffer().array());
                buffer = ByteBuffer.allocate(128 * 128 * Short.BYTES);
                channel.read(buffer);
                uint16V8 = uint16ArrayToInt32Array(buffer.asShortBuffer().array());
                gridIntHeightMultiplier = (mapHeightHeader.gridMaxHeight() - mapHeightHeader.gridHeight()) / 65535;
                gridGetHeightFunc = this::getHeightFromUint16;

            } else if (Flags.hasFlag(mapHeightHeader.flags(), MapHeightHeader.HeightAsInt8)) {
                buffer = ByteBuffer.allocate(129 * 129);
                channel.read(buffer);
                ubyteV9 = uint8ArrayToInt16Array(buffer.array());
                buffer = ByteBuffer.allocate(128 * 128);
                channel.read(buffer);
                ubyteV8 = uint8ArrayToInt16Array(buffer.array());

                gridIntHeightMultiplier = (mapHeightHeader.gridMaxHeight() - mapHeightHeader.gridHeight()) / 255;
                gridGetHeightFunc = this::getHeightFromUint8;
            } else {

                buffer = ByteBuffer.allocate(129 * 129 * Float.BYTES);
                channel.read(buffer);
                V9 = buffer.flip().asFloatBuffer().array();
                buffer = ByteBuffer.allocate(128 * 128 * Float.BYTES);
                channel.read(buffer);
                V8 = buffer.flip().asFloatBuffer().array();
                gridGetHeightFunc = this::getHeightFromFloat;
            }
        } else {
            gridGetHeightFunc = this::getHeightFromFlat;
        }

        if (Flags.hasFlag(mapHeightHeader.flags(), MapHeightHeader.HasFlightBounds)) {

            buffer = ByteBuffer.allocate(9 * Short.BYTES);
            channel.read(buffer);

            buffer = ByteBuffer.allocate(9 * Short.BYTES);
            channel.read(buffer);
            short[] minHeights = buffer.asShortBuffer().array();

            _minHeightPlanes = new Plane[8];

            for (int quarterIndex = 0; quarterIndex < 8; ++quarterIndex)
                _minHeightPlanes[quarterIndex] = new Plane(new Vector3(BOUND_GRID_COORDINATE[INDICES[quarterIndex][0]][0], BOUND_GRID_COORDINATE[INDICES[quarterIndex][0]][1], minHeights[INDICES[quarterIndex][0]]),
                        new Vector3(BOUND_GRID_COORDINATE[INDICES[quarterIndex][1]][0], BOUND_GRID_COORDINATE[INDICES[quarterIndex][1]][1], minHeights[INDICES[quarterIndex][1]]),
                        new Vector3(BOUND_GRID_COORDINATE[INDICES[quarterIndex][2]][0], BOUND_GRID_COORDINATE[INDICES[quarterIndex][2]][1], minHeights[INDICES[quarterIndex][2]]));
        }

        return true;
    }

    private boolean loadLiquidData(FileChannel channel, int offset) throws IOException {

        channel.position(offset);
        ByteBuffer buffer = ByteBuffer.allocate(MapLiquidHeader.BYTES);
        channel.read(buffer);
        buffer.flip();


        MapLiquidHeader liquidHeader = new MapLiquidHeader(buffer);

        if (liquidHeader.fourcc() != MAP_LIQUID_MAGIC)
            return false;

        liquidGlobalEntry = liquidHeader.liquidType();
        liquidGlobalFlags = LiquidHeaderTypeFlag.valueOf(liquidHeader.liquidFlags());
        liquidOffX = liquidHeader.offsetX();
        liquidOffY = liquidHeader.offsetY();
        liquidWidth = liquidHeader.width();
        liquidHeight = liquidHeader.height();
        liquidLevel = liquidHeader.liquidLevel();

        if (!Flags.hasFlag(liquidHeader.flags(), MapLiquidHeader.FLAG_NO_TYPE)) {
            buffer = ByteBuffer.allocate(16 * 16 * Short.BYTES);
            channel.read(buffer);
            liquidEntry = buffer.asShortBuffer().array();

            buffer = ByteBuffer.allocate(16 * 16);
            channel.read(buffer);
            liquidFlags = buffer.array();
        }

        if (!Flags.hasFlag(liquidHeader.flags(), MapLiquidHeader.FLAG_NO_HEIGHT)) {
            buffer = ByteBuffer.allocate(liquidWidth * liquidHeight * Float.BYTES);
            channel.read(buffer);
            liquidMap = buffer.asFloatBuffer().array();
        }
        return true;
    }

    private boolean loadHolesData(FileChannel channel, int offset) throws IOException {

        channel.position(offset);
        ByteBuffer buffer = ByteBuffer.allocate(16 * 16 * 8);
        channel.read(buffer);
        holes = buffer.array();
        return true;
    }

    private float getHeightFromFlat(float x, float y) {
        return gridHeight;
    }

    private float getHeightFromFloat(float x, float y) {
        if (uint16V8 == null || uint16V9 == null)
            return gridHeight;

        x = MAP_RESOLUTION * (CENTER_GRID_ID - x / SIZE_OF_GRIDS);
        y = MAP_RESOLUTION * (CENTER_GRID_ID - y / SIZE_OF_GRIDS);

        int x_int = (int) x;
        int y_int = (int) y;
        x -= x_int;
        y -= y_int;
        x_int &= MAP_RESOLUTION - 1;
        y_int &= MAP_RESOLUTION - 1;

        if (isHole(x_int, y_int))
            return INVALID_HEIGHT;

        // Height stored as: h5 - its v8 grid, h1-h4 - its v9 grid
        // +--------------> X
        // | h1-------h2     Coordinates is:
        // | | \  1  / |     h1 0, 0
        // | |  \   /  |     h2 0, 1
        // | | 2  h5 3 |     h3 1, 0
        // | |  /   \  |     h4 1, 1
        // | | /  4  \ |     h5 1/2, 1/2
        // | h3-------h4
        // V Y
        // For find height need
        // 1 - detect triangle
        // 2 - solve linear equation from triangle points
        // Calculate coefficients for solve h = a*x + b*y + c

        float a, b, c;

        if (x + y < 1) {
            if (x > y) {
                // 1 triangle (h1, h2, h5 points)
                float h1 = V9[x_int * 129 + y_int];
                float h2 = V9[(x_int + 1) * 129 + y_int];
                float h5 = 2 * V8[x_int * 128 + y_int];
                a = h2 - h1;
                b = h5 - h1 - h2;
                c = h1;
            } else {
                // 2 triangle (h1, h3, h5 points)
                float h1 = V9[x_int * 129 + y_int];
                float h3 = V9[x_int * 129 + y_int + 1];
                float h5 = 2 * V8[x_int * 128 + y_int];
                a = h5 - h1 - h3;
                b = h3 - h1;
                c = h1;
            }
        } else {
            if (x > y) {
                // 3 triangle (h2, h4, h5 points)
                float h2 = V9[(x_int + 1) * 129 + y_int];
                float h4 = V9[(x_int + 1) * 129 + y_int + 1];
                float h5 = 2 * V8[x_int * 128 + y_int];
                a = h2 + h4 - h5;
                b = h4 - h2;
                c = h5 - h4;
            } else {
                // 4 triangle (h3, h4, h5 points)
                float h3 = V9[x_int * 129 + y_int + 1];
                float h4 = V9[(x_int + 1) * 129 + y_int + 1];
                float h5 = 2 * V8[x_int * 128 + y_int];
                a = h4 - h3;
                b = h3 + h4 - h5;
                c = h5 - h4;
            }
        }

        // Calculate height
        return a * x + b * y + c;
    }

    private float getHeightFromUint8(float x, float y) {
        if (ubyteV8 == null || ubyteV9 == null)
            return gridHeight;

        x = MAP_RESOLUTION * (CENTER_GRID_ID - x / SIZE_OF_GRIDS);
        y = MAP_RESOLUTION * (CENTER_GRID_ID - y / SIZE_OF_GRIDS);

        int x_int = (int) x;
        int y_int = (int) y;
        x -= x_int;
        y -= y_int;
        x_int &= (MAP_RESOLUTION - 1);
        y_int &= (MAP_RESOLUTION - 1);

        if (isHole(x_int, y_int))
            return INVALID_HEIGHT;

        int a, b, c;


        int v9_h1_ptr = x_int * 128 + x_int + y_int;
        if (x + y < 1) {
            if (x > y) {
                // 1 triangle (h1, h2, h5 points)
                int h1 = ubyteV9[v9_h1_ptr];
                int h2 = ubyteV9[v9_h1_ptr + 129];
                int h5 = 2 * ubyteV8[x_int * 128 + y_int];
                a = h2 - h1;
                b = h5 - h1 - h2;
                c = h1;
            } else {
                // 2 triangle (h1, h3, h5 points)
                int h1 = ubyteV9[v9_h1_ptr];
                int h3 = ubyteV9[v9_h1_ptr + 1];
                int h5 = 2 * ubyteV8[x_int * 128 + y_int];
                a = h5 - h1 - h3;
                b = h3 - h1;
                c = h1;
            }
        } else {
            if (x > y) {
                // 3 triangle (h2, h4, h5 points)
                int h2 = ubyteV9[v9_h1_ptr + 129];
                int h4 = ubyteV9[v9_h1_ptr + 130];
                int h5 = 2 * ubyteV8[x_int * 128 + y_int];
                a = h2 + h4 - h5;
                b = h4 - h2;
                c = h5 - h4;
            } else {
                // 4 triangle (h3, h4, h5 points)
                int h3 = ubyteV9[v9_h1_ptr + 1];
                int h4 = ubyteV9[v9_h1_ptr + 130];
                int h5 = 2 * ubyteV8[x_int * 128 + y_int];
                a = h4 - h3;
                b = h3 + h4 - h5;
                c = h5 - h4;
            }
        }
        // Calculate height
        return ((a * x) + (b * y) + c) * gridIntHeightMultiplier + gridHeight;

    }

    private float getHeightFromUint16(float x, float y) {
        if (uint16V8 == null || uint16V9 == null)
            return gridHeight;

        x = MAP_RESOLUTION * (CENTER_GRID_ID - x / SIZE_OF_GRIDS);
        y = MAP_RESOLUTION * (CENTER_GRID_ID - y / SIZE_OF_GRIDS);

        int x_int = (int) x;
        int y_int = (int) y;
        x -= x_int;
        y -= y_int;
        x_int &= MAP_RESOLUTION - 1;
        y_int &= MAP_RESOLUTION - 1;

        if (isHole(x_int, y_int))
            return INVALID_HEIGHT;

        int a, b, c;

        int V9_h1_ptr = x_int * 128 + x_int + y_int;
        if (x + y < 1) {
            if (x > y) {
                // 1 triangle (h1, h2, h5 points)
                int h1 = uint16V9[V9_h1_ptr];
                int h2 = uint16V9[V9_h1_ptr + 129];
                int h5 = 2 * uint16V8[x_int * 128 + y_int];
                a = h2 - h1;
                b = h5 - h1 - h2;
                c = h1;
            } else {
                // 2 triangle (h1, h3, h5 points)
                int h1 = uint16V9[V9_h1_ptr];
                int h3 = uint16V9[V9_h1_ptr + 1];
                int h5 = 2 * (uint16V8[x_int * 128 + y_int]);
                a = h5 - h1 - h3;
                b = h3 - h1;
                c = h1;
            }
        } else {
            if (x > y) {
                // 3 triangle (h2, h4, h5 points)
                int h2 = uint16V9[V9_h1_ptr + 129];
                int h4 = uint16V9[V9_h1_ptr + 130];
                int h5 = 2 * (uint16V8[x_int * 128 + y_int]);
                a = h2 + h4 - h5;
                b = h4 - h2;
                c = h5 - h4;
            } else {
                // 4 triangle (h3, h4, h5 points)
                int h3 = uint16V9[V9_h1_ptr + 1];
                int h4 = uint16V9[V9_h1_ptr + 130];
                int h5 = 2 * (uint16V8[x_int * 128 + y_int]);
                a = h4 - h3;
                b = h3 + h4 - h5;
                c = h5 - h4;
            }
        }
        // Calculate height
        return ((a * x) + (b * y) + c) * gridIntHeightMultiplier + gridHeight;

    }

    private boolean isHole(int row, int col) {
        if (holes == null)
            return false;

        var cellRow = row / 8; // 8 squares per cell
        var cellCol = col / 8;
        var holeRow = row % 8;
        var holeCol = col % 8;

        return (holes[cellRow * 16 * 8 + cellCol * 8 + holeRow] & 1 << holeCol) != 0;
    }


    private int[] uint16ArrayToInt32Array(short[] array) {
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] & 0xffff;
        }
        return result;
    }

    private short[] uint8ArrayToInt16Array(byte[] array) {
        short[] result = new short[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = (short) (array[i] & 0xff);
        }
        return result;
    }
}
