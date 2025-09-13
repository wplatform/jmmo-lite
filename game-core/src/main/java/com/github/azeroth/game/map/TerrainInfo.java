package com.github.azeroth.game.map;


import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.common.LocalizedString;
import com.github.azeroth.common.Logs;
import com.github.azeroth.common.YieldResult;
import com.github.azeroth.dbc.DbcObjectManager;
import com.github.azeroth.dbc.defines.AreaFlag;
import com.github.azeroth.dbc.domain.AreaTable;
import com.github.azeroth.dbc.domain.LiquidType;
import com.github.azeroth.dbc.domain.MapEntry;
import com.github.azeroth.dbc.domain.WmoAreaTable;
import com.github.azeroth.game.domain.map.*;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.map.collision.DynamicMapTree;
import com.github.azeroth.game.map.collision.VMapManager;
import com.github.azeroth.game.domain.map.enums.LiquidHeaderTypeFlag;
import com.github.azeroth.game.domain.map.enums.LoadResult;
import com.github.azeroth.game.domain.map.enums.ZLiquidStatus;
import com.github.azeroth.game.map.grid.GridMap;
import com.github.azeroth.game.domain.phasing.PhaseShift;
import com.github.azeroth.game.phasing.PhasingHandler;
import com.github.azeroth.game.world.setting.WorldSetting;
import com.github.azeroth.utils.MathUtil;
import com.github.azeroth.utils.StringUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import static com.github.azeroth.defines.SharedDefine.GROUND_HEIGHT_TOLERANCE;
import static com.github.azeroth.defines.SharedDefine.Z_OFFSET_FIND_HEIGHT;
import static com.github.azeroth.game.domain.map.MapDefine.*;


@Getter
@RequiredArgsConstructor
public class TerrainInfo {

    private static final Duration CLEANUP_INTERVAL = Duration.ofMinutes(1);
    private final int mapId;
    private final boolean keepLoaded = false;
    private final ArrayList<TerrainInfo> childTerrain = new ArrayList<>();
    private final ReentrantLock loadLock = new ReentrantLock();
    private final GridMap[][] gridMap = new GridMap[MAX_NUMBER_OF_GRIDS][MAX_NUMBER_OF_GRIDS];
    private final AtomicIntegerArray referenceCountFromMap = new AtomicIntegerArray(new int[MAX_NUMBER_OF_GRIDS * MAX_NUMBER_OF_GRIDS]);
    private final BitSet loadedGrids = new BitSet(MAX_NUMBER_OF_GRIDS * MAX_NUMBER_OF_GRIDS);
    private final BitSet gridFileExists = new BitSet(MAX_NUMBER_OF_GRIDS * MAX_NUMBER_OF_GRIDS);
    private final ApplicationContext applicationContext;
    private final VMapManager vMapManager;
    private final MMapManager mMapManager;
    private final DbcObjectManager dbcObjectManager;
    private final WorldSetting worldSetting;
    private LocalizedString name;
    private TerrainInfo parentTerrain;

    public static boolean isNotInWMOInterior(int mogpFlags) {
        return (mogpFlags & 0x2000) == 0;
    }

    private static int indexFor(int gx, int gy) {
        return gx * MAX_NUMBER_OF_GRIDS + gy;
    }

    public String getMapName() {
        if (name == null) {
            MapEntry map = dbcObjectManager.map(mapId);
            name = map.getMapName();
        }
        return LocalizedString.get(name, worldSetting.dbcLocale);
    }

    public void discoverGridMapFiles() {

        Path tileListPath = worldSetting.dataDir.resolve(StringUtil.format("maps/{:04}.tilelist", mapId));

        // tile list is optional
        if (Files.exists(tileListPath)) {
            try (FileChannel fileChannel = FileChannel.open(tileListPath, StandardOpenOption.READ)) {
                ByteBuffer buffer = ByteBuffer.allocate(12);
                fileChannel.read(buffer);
                buffer.flip();
                int mapMagic = buffer.getInt();
                int versionMagic = buffer.getInt();
                if (mapMagic == MAP_MAGIC && versionMagic == MAP_VERSION_MAGIC) {
                    buffer = ByteBuffer.allocate(MAX_NUMBER_OF_GRIDS * MAX_NUMBER_OF_GRIDS);
                    fileChannel.read(buffer);
                    byte[] tilesData = buffer.array();

                    for (var gx = 0; gx < MAX_NUMBER_OF_GRIDS; ++gx)
                        for (var gy = 0; gy < MAX_NUMBER_OF_GRIDS; ++gy)
                            gridFileExists.set(indexFor(gx, gy), tilesData[indexFor(gx, gy)] == 49);
                    return;
                }
            } catch (IOException e) {
                Logs.MAPS.error("Error loading tile list file {} error.", tileListPath.toAbsolutePath(), e);
            }
        }

        for (int gx = 0; gx < MAX_NUMBER_OF_GRIDS; ++gx)
            for (int gy = 0; gy < MAX_NUMBER_OF_GRIDS; ++gy)
                gridFileExists.set(indexFor(gx, gy), existMap(mapId, gx, gy, false));

    }

    public boolean existMap(int mapId, int gx, int gy) {
        return existMap(mapId, gx, gy, true);
    }

    public boolean existMap(int mapId, int gx, int gy, boolean logging) {
        Path mapFilePath = worldSetting.dataDir.resolve(StringUtil.format("maps/{:04}_{:02}_{:02}.map", mapId, gx, gy));
        boolean ret = false;
        if (!Files.exists(mapFilePath)) {
            try (FileChannel fileChannel = FileChannel.open(mapFilePath, StandardOpenOption.READ)) {
                ByteBuffer buffer = ByteBuffer.allocate(MapFileHeader.BYTES);
                fileChannel.read(buffer);
                buffer.flip();
                MapFileHeader header = new MapFileHeader(buffer);
                if (header.mapMagic() != MAP_MAGIC || header.versionMagic() != MAP_VERSION_MAGIC) {
                    if (logging) {
                        Logs.MAPS.error("Map file '{}' is from an incompatible map version ({} v{}), {} v{} is expected. Please pull your source, " +
                                        "recompile tools and recreate maps using the updated mapextractor, then replace your old map files with new files. " +
                                        "If you still have problems search on forum for error TCE00018."
                                , mapFilePath.toAbsolutePath(), header.mapMagic(), header.versionMagic(), MAP_MAGIC, MAP_VERSION_MAGIC);
                    }

                } else {
                    ret = true;
                }
            } catch (IOException e) {
                if (logging) {
                    Logs.MAPS.error("Error read map file {} error.", mapFilePath.toAbsolutePath(), e);
                }
            }
        } else {
            if (logging) {
                Logs.MAPS.error("Map file '{}' does not exist!", mapFilePath.toAbsolutePath());
                Logs.MAPS.error("Please place MAP-files (*.map) in the appropriate directory ({}maps/), or correct the DataDir setting in your worldserver.conf file.", mapFilePath.getParent().toAbsolutePath());
            }
        }
        return ret;
    }

    public boolean existVMap(int mapid, int gx, int gy) {
        if (vMapManager.isMapLoadingEnabled()) {
            Path vmtreePath = worldSetting.dataDir.resolve(StringUtil.format("vmaps/{:04}/{:04}.vmtree", gx, gy));
            LoadResult result = vMapManager.existsMap(mapid, gx, gy);
            switch (result) {
                case Success:
                    break;
                case FileNotFound:
                    Logs.MAPS.error("VMap file '{}' does not exist", vmtreePath.toAbsolutePath());
                    Logs.MAPS.error("Please place VMAP files (*.vmtree and *.vmtile) in the vmap directory ({}), or correct the DataDir setting in your worldserver.conf file.", vmtreePath.getParent().toAbsolutePath());
                    return false;
                case VersionMismatch:
                    Logs.MAPS.error("VMap file '{}' couldn't be loaded", vmtreePath.toAbsolutePath());
                    Logs.MAPS.error("This is because the version of the VMap file and the version of this module are different, please re-extract the maps with the tools compiled with this module.");
                    return false;
                case ReadFromFileFailed:
                    Logs.MAPS.error("VMap file '{}' couldn't be loaded", vmtreePath.toAbsolutePath());
                    Logs.MAPS.error("This is because VMAP files are corrupted, please re-extract the maps with the tools compiled with this module.");
                    return false;
                case DisabledInConfig:
                    Logs.MAPS.error("VMap file '{}' couldn't be loaded", vmtreePath.toAbsolutePath());
                    Logs.MAPS.error("This is because VMAP is disabled in config file.");
                    return false;
            }
        }

        return true;
    }

    public boolean hasChildTerrainGridFile(int mapId, int gx, int gy) {
        return childTerrain.stream()
                .filter(e -> e.mapId == mapId)
                .findFirst()
                .map(e -> e.gridFileExists.get(indexFor(gx, gy)))
                .orElse(false);
    }

    public void addChildTerrain(TerrainInfo terrain) {
        terrain.parentTerrain = this;
        childTerrain.add(terrain);
    }

    public void loadMapAndVMap(int gx, int gy) {

        if (referenceCountFromMap.incrementAndGet(indexFor(gx, gy)) != 1)    // check if already loaded
            return;
        try {
            loadLock.lock();
            loadMapAndVMap0(gx, gy);
        } finally {
            loadLock.unlock();
        }
    }

    private void loadMapAndVMap0(int gx, int gy) {
        loadMap(gx, gy);
        loadVMap(gx, gy);
        loadMMap(gx, gy);

        for (TerrainInfo terrainInfo : childTerrain) {
            terrainInfo.loadMapAndVMap0(gx, gy);
        }
        loadedGrids.set(indexFor(gx, gy), true);
    }

    public void loadMap(int gx, int gy) {
        if (gridMap[gx][gy] != null)
            return;

        if (!gridFileExists.get(indexFor(gx, gy)))
            return;

        // map file name
        Path mapFilePath = worldSetting.dataDir.resolve(StringUtil.format("maps/{:04}_{:02}_{:02}.map", mapId, gx, gy));
        Logs.MAPS.info("Loading map {}", mapFilePath.toAbsolutePath());

        // loading data
        GridMap newGridMap = createGridMap();
        var gridMapLoadResult = newGridMap.loadData(mapFilePath);

        if (gridMapLoadResult == LoadResult.Success)
            gridMap[gx][gy] = newGridMap;
        else
            gridFileExists.set(indexFor(gx, gy), false);

    }

    private GridMap createGridMap() {
        return applicationContext.getBean(GridMap.class);
    }

    private void loadVMap(int gx, int gy) {
        // x and y are swapped !!
        LoadResult vmapLoadResult = vMapManager.loadMap(mapId, gx, gy);
        switch (vmapLoadResult) {
            case Success:
                Logs.MAPS.debug("VMAP loaded name:{}, id:{}, x:{}, y:{} (vmap rep.: x:{}, y:{})", getMapName(), mapId, gx, gy, gx, gy);
                break;
            case VersionMismatch:
            case ReadFromFileFailed:
                Logs.MAPS.error("Could not load VMAP name:{}, id:{}, x:{}, y:{} (vmap rep.: x:{}, y:{})", getMapName(), mapId, gx, gy, gx, gy);
                break;
            case DisabledInConfig:
                Logs.MAPS.debug("Ignored VMAP name:{}, id:{}, x:{}, y:{} (vmap rep.: x:{}, y:{})", getMapName(), mapId, gx, gy, gx, gy);
                break;
            default:
                break;
        }
    }

    private void loadMMap(int gx, int gy) {

        boolean mMapLoadResult = mMapManager.loadMap(mapId, gx, gy);

        if (mMapLoadResult)
            Logs.MMAPS_TILES.debug("MMAP loaded name:{}, id:{}, x:{}, y:{} (mmap rep.: x:{}, y:{})", getMapName(), mapId, gx, gy, gx, gy);
        else
            Logs.MMAPS_TILES.debug("Could not load MMAP name:{}, id:{}, x:{}, y:{} (mmap rep.: x:{}, y:{})", getMapName(), mapId, gx, gy, gx, gy);

    }

    public void unloadMap(int gx, int gy) {
        int refCount = referenceCountFromMap.decrementAndGet(indexFor(gx, gy));
        if (refCount < 1) {
            releaseMap(gx, gy);
        }
    }

    private void releaseMap(int gx, int gy) {
        gridMap[gx][gy] = null;
        vMapManager.unloadMap(mapId, gx, gy);
        mMapManager.unloadMap(mapId, gx, gy);
        for (TerrainInfo child : childTerrain) {
            child.releaseMap(gx, gy);
        }
        loadedGrids.set(indexFor(gx, gy), false);
    }

    public GridMap getGrid(int mapId, float x, float y) {
        return getGrid(mapId, x, y, true);
    }

    public GridMap getGrid(int mapId, float x, float y, boolean loadIfMissing) {
        // half opt method
        int gx = (int) (CENTER_GRID_ID - x / SIZE_OF_GRIDS);                   //grid x
        int gy = (int) (CENTER_GRID_ID - y / SIZE_OF_GRIDS);                   //grid y

        // ensure GridMap is loaded
        if (!loadedGrids.get(indexFor(gx, gy)) && loadIfMissing) {
            try {
                loadLock.lock();
                loadMapAndVMap0(gx, gy);
            } finally {
                loadLock.unlock();
            }

        }

        GridMap grid = gridMap[gx][gy];

        if (this.mapId != mapId) {
            Optional<TerrainInfo> optionalTerrainInfo = childTerrain.stream()
                    .filter(e -> e.mapId == mapId)
                    .findFirst();
            if (optionalTerrainInfo.isPresent() && optionalTerrainInfo.get().gridMap[gx][gy] != null) {
                grid = optionalTerrainInfo.get().getGrid(mapId, x, y, false);
            }
        }

        return grid;
    }

    public void cleanUpGrids(int diff) {
        // delete those GridMap objects which have refcount = 0
        for (int x = 0; x < MAX_NUMBER_OF_GRIDS; ++x)
            for (int y = 0; y < MAX_NUMBER_OF_GRIDS; ++y)
                if (loadedGrids.get(indexFor(x, y)) && referenceCountFromMap.get(indexFor(x, y)) < 1)
                    releaseMap(x, y);
    }

    public PositionFullTerrainStatus getFullTerrainStatusForPosition(PhaseShift phaseShift, int mapId, Position pos,
                                                                     Byte reqLiquidType, float collisionHeight,
                                                                     DynamicMapTree dynamicMapTree) {

        PositionFullTerrainStatus data = new PositionFullTerrainStatus();

        AreaInfo wmoData = null;


        int terrainMapId = PhasingHandler.getTerrainMapId(phaseShift, mapId, this, pos.getX(), pos.getY());
        GridMap gmap = getGrid(terrainMapId, pos.getX(), pos.getY());
        AreaInfo vmapData = vMapManager.getAreaAndLiquidData(terrainMapId, pos, reqLiquidType);
        AreaInfo dynData = dynamicMapTree != null ? dynamicMapTree.getAreaAndLiquidData(pos, phaseShift, reqLiquidType) : null;

        int gridAreaId = 0;
        var gridMapHeight = INVALID_HEIGHT;

        if (gmap != null) {
            gridAreaId = gmap.getArea(pos.getX(), pos.getY());
            gridMapHeight = gmap.getHeight(pos.getX(), pos.getY());
        }

        boolean useGridLiquid = true;


        // floor is the height we are closer to (but only if above)
        data.setFloorZ(VMAP_INVALID_HEIGHT);
        if (gridMapHeight > INVALID_HEIGHT && MathUtil.fuzzyGe(pos.getZ(), gridMapHeight - GROUND_HEIGHT_TOLERANCE))
            data.setFloorZ(gridMapHeight);

        if (vmapData.result && vmapData.getFloorZ() > VMAP_INVALID_HEIGHT &&
                MathUtil.fuzzyGe(pos.getZ(), vmapData.getFloorZ() - GROUND_HEIGHT_TOLERANCE) &&
                (MathUtil.fuzzyLt(pos.getZ(), gridMapHeight - GROUND_HEIGHT_TOLERANCE) || vmapData.getFloorZ() > gridMapHeight)) {
            data.setFloorZ(vmapData.getFloorZ());
            wmoData = vmapData;
        }


        // NOTE: Objects will not detect a case when a wmo providing area/liquid despawns from under them
        // but this is fine as these kind of objects are not meant to be spawned and despawned a lot
        // example: Lich King platform
        if (dynData != null && dynData.result &&
                dynData.getFloorZ() > VMAP_INVALID_HEIGHT &&
                MathUtil.fuzzyGe(pos.getZ(), dynData.getFloorZ() - GROUND_HEIGHT_TOLERANCE) &&
                (MathUtil.fuzzyLt(pos.getZ(), gridMapHeight - GROUND_HEIGHT_TOLERANCE) || dynData.getFloorZ() > gridMapHeight) &&
                (MathUtil.fuzzyLt(pos.getZ(), vmapData.getFloorZ() - GROUND_HEIGHT_TOLERANCE) || dynData.getFloorZ() > vmapData.getFloorZ())) {
            data.setFloorZ(dynData.getFloorZ());
            wmoData = dynData;
        }

        if (wmoData != null) {
            data.setWmoLocation(new WmoLocation(wmoData.getAdtId(), wmoData.getRootId(), wmoData.getGroupId(), wmoData.getFlags()));
            // wmo found
            WmoAreaTable wmoEntry = dbcObjectManager.getWMOAreaTable(wmoData.getRootId(), wmoData.getAdtId(), wmoData.getGroupId());

            if (wmoEntry == null)
                wmoEntry = dbcObjectManager.getWMOAreaTable(wmoData.getRootId(), wmoData.getAdtId(), -1);

            data.setOutdoors((wmoData.getFlags() & 0x8) != 0);

            if (wmoEntry != null) {
                data.setAreaId(wmoEntry.getAreaTableID());

                if ((wmoEntry.getFlags() & 4) != 0)
                    data.setOutdoors(true);
                else if ((wmoEntry.getFlags() & 2) != 0)
                    data.setOutdoors(false);
            }

            if (data.getAreaId() == 0)
                data.setAreaId(gridAreaId);

            useGridLiquid = isNotInWMOInterior(wmoData.getFlags());
        } else {
            data.setOutdoors(true);
            data.setAreaId(gridAreaId);
            AreaTable areaEntry = dbcObjectManager.areaTable(data.getAreaId());

            if (areaEntry != null) {
                EnumFlag<AreaFlag> areFlags = areaEntry.getFlags();
                data.setOutdoors(areFlags.hasFlag(AreaFlag.ForceOutdoors) || !areFlags.hasFlag(AreaFlag.ForceIndoors));
            }

        }

        if (data.getAreaId() == 0)
            data.setAreaId(dbcObjectManager.map(getMapId()).getAreaTableID());

        AreaTable areaEntry = dbcObjectManager.areaTable(data.getAreaId());


        // liquid processing
        data.setLiquidStatus(EnumFlag.of(ZLiquidStatus.NO_WATER));

        if (wmoData != null && wmoData.liquidEnabled && wmoData.getLiquidLevel() > wmoData.getFloorZ()) {
            int liquidType = wmoData.getLiquidType();

            if (getMapId() == 530 && liquidType == 2) // gotta love hacks
                liquidType = 15;

            int liquidFlagType = 0;

            LiquidType liquidTypeEntry = dbcObjectManager.liquidType(liquidType);
            if (liquidTypeEntry != null) {
                liquidFlagType = liquidTypeEntry.getSoundBank();
            }


            if (liquidType != 0 && liquidType < 21 && areaEntry != null) {
                int overrideLiquid = areaEntry.getLiquidTypeID(liquidFlagType);

                if (overrideLiquid == 0 && areaEntry.getParentAreaID() != 0) {
                    AreaTable zoneEntry = dbcObjectManager.areaTable(areaEntry.getParentAreaID().intValue());
                    if (zoneEntry != null)
                        overrideLiquid = zoneEntry.getLiquidTypeID(liquidFlagType);
                }

                LiquidType overrideData = dbcObjectManager.liquidType(overrideLiquid);
                if (overrideData != null) {
                    liquidType = overrideLiquid;
                    liquidFlagType = overrideData.getSoundBank();
                }
            }

            LiquidData liquidInfo = new LiquidData();
            liquidInfo.setLevel(wmoData.getLiquidLevel());
            liquidInfo.setDepthLevel(wmoData.getFloorZ());
            liquidInfo.setEntry(liquidType);
            liquidInfo.setTypeFlags(EnumFlag.of(LiquidHeaderTypeFlag.valueOf(1 << liquidFlagType)));
            data.setLiquidInfo(liquidInfo);


            float delta = wmoData.getLiquidLevel() - pos.getZ();

            EnumFlag<ZLiquidStatus> status = EnumFlag.of(ZLiquidStatus.ABOVE_WATER);
            if (delta > collisionHeight)
                status.set(ZLiquidStatus.UNDER_WATER);
            else if (delta > 0.0f)
                status.set(ZLiquidStatus.IN_WATER);
            else if (delta > -0.1f)
                status.set(ZLiquidStatus.WATER_WALK);

            if (!status.equals(ZLiquidStatus.ABOVE_WATER))
                if (Math.abs(wmoData.getFloorZ() - pos.getZ()) <= GROUND_HEIGHT_TOLERANCE)
                    status.addFlag(ZLiquidStatus.OCEAN_FLOOR);

            data.setLiquidStatus(status);


        }

        // look up liquid data from grid map
        if (gmap != null && useGridLiquid) {
            LiquidData gridMapLiquid = new LiquidData();
            var gridMapStatus = gmap.getLiquidStatus(pos.getX(), pos.getY(), pos.getZ(), reqLiquidType, gridMapLiquid, collisionHeight);

            if (!gridMapStatus.equals(ZLiquidStatus.NO_WATER) && (wmoData == null || gridMapLiquid.getDepthLevel() > wmoData.getFloorZ())) {
                if (getMapId() == 530 && gridMapLiquid.getEntry() == 2)
                    gridMapLiquid.setEntry(15);

                data.setLiquidInfo(gridMapLiquid);
                data.setLiquidStatus(gridMapStatus);
            }
        }

        return data;
    }

    public EnumFlag<ZLiquidStatus> getLiquidStatus(PhaseShift phaseShift, int mapId, Position pos, Byte reqLiquidType, LiquidData data, float collisionHeight) {

        EnumFlag<ZLiquidStatus> result = EnumFlag.of(ZLiquidStatus.NO_WATER);

        boolean useGridLiquid = true;
        int terrainMapId = PhasingHandler.getTerrainMapId(phaseShift, mapId, this, pos.getX(), pos.getY());
        AreaInfo vmapData = vMapManager.getAreaAndLiquidData(terrainMapId, pos, reqLiquidType);
        if (vmapData.liquidEnabled) {
            int liquidType = vmapData.liquidType;
            float liquidLevel = vmapData.liquidLevel;
            useGridLiquid = !vmapData.result || isNotInWMOInterior(vmapData.flags);
            Logs.MAPS.debug("GetLiquidStatus(): vmap liquid level: {} ground: {} type: {}", liquidLevel, vmapData.getFloorZ(), liquidType);
            // Check water level and ground level
            if (liquidLevel > vmapData.getFloorZ() && MathUtil.fuzzyGe(pos.getZ(), vmapData.getFloorZ() - GROUND_HEIGHT_TOLERANCE)) {
                // All ok in water -> store data
                if (data != null) {
                    // hardcoded in client like this
                    if (this.mapId == 530 && liquidType == 2)
                        vmapData.liquidType = 15;

                    int liquidFlagType = 0;
                    LiquidType liq = dbcObjectManager.liquidType(liquidType);
                    if (liq != null) {
                        liquidFlagType = liq.getSoundBank();
                    }

                    if (vmapData.liquidType != 0 && liquidType < 21) {
                        AreaTable area = dbcObjectManager.areaTable(getAreaId(phaseShift, mapId, pos, null));
                        if (area != null) {
                            int overrideLiquid = area.getLiquidTypeID(liquidFlagType);
                            if (overrideLiquid == 0 && area.getParentAreaID() != 0) {
                                area = dbcObjectManager.areaTable(area.getParentAreaID().intValue());
                                if (area != null)
                                    overrideLiquid = area.getLiquidTypeID(liquidFlagType);
                            }
                            LiquidType overrideLiquidType = dbcObjectManager.liquidType(overrideLiquid);
                            if (overrideLiquidType != null) {
                                vmapData.liquidType = overrideLiquid;
                                liquidFlagType = overrideLiquidType.getSoundBank();
                            }
                        }
                    }

                    data.setLevel(liquidLevel);
                    data.setDepthLevel(vmapData.getFloorZ());
                    data.setEntry(liquidType);
                    data.setTypeFlags(EnumFlag.of(LiquidHeaderTypeFlag.valueOf(1 << liquidFlagType)));
                }

                float delta = liquidLevel - pos.getZ();

                // Get position delta
                EnumFlag<ZLiquidStatus> status = EnumFlag.of(ZLiquidStatus.ABOVE_WATER);
                if (delta > collisionHeight)            // Under water
                    status.set(ZLiquidStatus.UNDER_WATER);
                else if (delta > 0.0f)                  // In water
                    status.set(ZLiquidStatus.IN_WATER);

                else if (delta > -0.1f)                 // Walk on water
                    status.set(ZLiquidStatus.WATER_WALK);

                if (!status.equals(ZLiquidStatus.ABOVE_WATER)) {
                    if (Math.abs(vmapData.getFloorZ() - pos.getZ()) <= GROUND_HEIGHT_TOLERANCE)
                        status.addFlag(ZLiquidStatus.OCEAN_FLOOR);

                    return status;
                }

                result.set(ZLiquidStatus.ABOVE_WATER);
            }
        }

        if (useGridLiquid) {
            GridMap gmap = getGrid(terrainMapId, pos.getX(), pos.getY());
            if (gmap != null) {
                LiquidData map_data = new LiquidData();
                EnumFlag<ZLiquidStatus> mapResult = gmap.getLiquidStatus(pos.getX(), pos.getY(), pos.getZ(), reqLiquidType, map_data, collisionHeight);
                // Not override ABOVE_WATER with NO_WATER:
                if (!mapResult.equals(ZLiquidStatus.NO_WATER) && (map_data.getLevel() > vmapData.getFloorZ())) {
                    if (data != null) {
                        // hardcoded in client like this
                        if (getMapId() == 530 && map_data.getEntry() == 2)
                            map_data.setEntry(15);
                        data.setLevel(map_data.getLevel());
                        data.setEntry(map_data.getEntry());
                        data.setTypeFlags(map_data.getTypeFlags());
                        data.setDepthLevel(map_data.getDepthLevel());
                    }
                    return mapResult;
                }
            }
        }
        return result;
    }

    public boolean getAreaInfo(PhaseShift phaseShift, int mapId, Position pos, AreaInfo areaInfo, DynamicMapTree dynamicMapTree) {

        float check_z = pos.getZ();

        int terrainMapId = PhasingHandler.getTerrainMapId(phaseShift, mapId, this, pos.getX(), pos.getY());



        AreaInfo vmapAreaInfo = vMapManager.getAreaAndLiquidData(terrainMapId, pos, null);
        AreaInfo dynamicAreaInfo = dynamicMapTree == null ? null : dynamicMapTree.getAreaAndLiquidData(pos, phaseShift, null);

        var hasVmapAreaInfo = vmapAreaInfo.result;
        var hasDynamicAreaInfo = dynamicAreaInfo != null && dynamicAreaInfo.result;


        Function<AreaInfo, Float> resultFetch = (source) -> {
            areaInfo.setAdtId(source.getAdtId());
            areaInfo.setFlags(source.getFlags());
            areaInfo.setGroupId(source.getGroupId());
            areaInfo.setFloorZ(source.floorZ);
            areaInfo.setRootId(source.getRootId());
            return source.getFloorZ();
        };

        if (hasVmapAreaInfo) {
            if (hasDynamicAreaInfo && dynamicAreaInfo.getFloorZ() > vmapAreaInfo.getFloorZ())
                check_z = resultFetch.apply(dynamicAreaInfo);
            else
                check_z = resultFetch.apply(vmapAreaInfo);
        } else if (dynamicAreaInfo != null) {
            check_z = resultFetch.apply(dynamicAreaInfo);
        }


        if (hasVmapAreaInfo || hasDynamicAreaInfo) {
            // check if there's terrain between player height and object height
            var gmap = getGrid(terrainMapId, pos.getX(), pos.getY());

            if (gmap != null) {
                var mapHeight = gmap.getHeight(pos.getX(), pos.getY());

                // z + 2.0f condition taken from GetHeight(), not sure if it's such a great choice...
                return !(pos.getZ() + 2.0f > mapHeight) || !(mapHeight > check_z);
            }

            return true;
        }

        return false;
    }


    public int getAreaId(PhaseShift phaseShift, int mapId, Position position, DynamicMapTree dynamicMapTree) {

        AreaInfo areaInfo = new AreaInfo();

        boolean hasVmapArea = getAreaInfo(phaseShift, mapId, position, areaInfo, dynamicMapTree);

        int gridAreaId = 0;
        float gridMapHeight = INVALID_HEIGHT;
        GridMap gmap = getGrid(PhasingHandler.getTerrainMapId(phaseShift, mapId, this, position.getX(), position.getY()), position.getX(), position.getY());

        if (gmap != null) {
            gridAreaId = gmap.getArea(position.getX(), position.getY());
            gridMapHeight = gmap.getHeight(position.getX(), position.getY());
        }

        int areaId = 0;

        // floor is the height we are closer to (but only if above)
        if (hasVmapArea && MathUtil.fuzzyGe(position.getZ(), position.getZ() - GROUND_HEIGHT_TOLERANCE)
                && (MathUtil.fuzzyLt(position.getZ(), gridMapHeight - GROUND_HEIGHT_TOLERANCE) || position.getZ() > gridMapHeight)) {
            // wmo found
            WmoAreaTable wmoEntry = dbcObjectManager.getWMOAreaTable(areaInfo.getRootId(), areaInfo.getAdtId(), areaInfo.getGroupId());

            if (wmoEntry != null)
                areaId = wmoEntry.getAreaTableID();

            if (areaId == 0)
                areaId = gridAreaId;
        } else {
            areaId = gridAreaId;
        }

        if (areaId == 0)
            areaId = dbcObjectManager.map(getMapId()).getAreaTableID();

        return areaId;
    }


    public int getZoneId(PhaseShift phaseShift, int mapId, Position position, DynamicMapTree dynamicMapTree) {
        int areaId = getAreaId(phaseShift, mapId, position, dynamicMapTree);
        AreaTable area = dbcObjectManager.areaTable(areaId);
        if (area != null && area.getParentAreaID() != 0 && area.getFlags().hasFlag(AreaFlag.IsSubzone))
            return area.getParentAreaID();

        return areaId;
    }

    public ZoneAndAreaId getZoneAndAreaId(PhaseShift phaseShift, int mapId, Position pos, DynamicMapTree dynamicMapTree) {
        int areaId = getAreaId(phaseShift, mapId, pos, dynamicMapTree);
        int zoneId = areaId;
        AreaTable area = dbcObjectManager.areaTable(areaId);

        if (area != null && area.getParentAreaID() != 0)
            zoneId = area.getParentAreaID();

        return ZoneAndAreaId.of(zoneId, areaId);
    }

    public float getMinHeight(PhaseShift phaseShift, int mapId, float x, float y) {
        var grid = getGrid(PhasingHandler.getTerrainMapId(phaseShift, mapId, this, x, y), x, y);

        if (grid != null)
            return grid.GetMinHeight(x, y);

        return -500.0f;
    }

    public float getGridHeight(PhaseShift phaseShift, int mapId, float x, float y) {
        GridMap gmap = getGrid(PhasingHandler.getTerrainMapId(phaseShift, mapId, this, x, y), x, y);

        if (gmap != null)
            return gmap.getHeight(x, y);

        return VMAP_INVALID_HEIGHT_VALUE;
    }

    public float getStaticHeight(PhaseShift phaseShift, int mapId, Position pos, boolean checkVMap, float maxSearchDist) {
        return getStaticHeight(phaseShift, mapId, pos.getX(), pos.getY(), pos.getZ(), checkVMap, maxSearchDist);
    }

    public float getStaticHeight(PhaseShift phaseShift, int mapId, float x, float y, float z, boolean checkVMap, float maxSearchDist) {
        // find raw .map surface under Z coordinates
        float mapHeight = MapDefine.VMAP_INVALID_HEIGHT_VALUE;
        var terrainMapId = PhasingHandler.getTerrainMapId(phaseShift, mapId, this, x, y);

        float gridHeight = getGridHeight(phaseShift, mapId, x, y);


        if (MathUtil.fuzzyGe(z, gridHeight - GROUND_HEIGHT_TOLERANCE))
            mapHeight = gridHeight;

        float vmapHeight = MapDefine.VMAP_INVALID_HEIGHT_VALUE;

        if (checkVMap && vMapManager.isHeightCalcEnabled())
            vmapHeight = vMapManager.getHeight(terrainMapId, x, y, z, maxSearchDist);

        // mapHeight set for any above raw ground Z or <= INVALID_HEIGHT
        // vmapheight set for any under Z second or <= INVALID_HEIGHT
        if (vmapHeight > INVALID_HEIGHT) {
            if (mapHeight > INVALID_HEIGHT) {
                // we have mapheight and vmapheight and must select more appropriate

                // vmap height above map height
                // or if the distance of the vmap height is less the land height distance
                if (vmapHeight > mapHeight || Math.abs(mapHeight - z) > Math.abs(vmapHeight - z))
                    return vmapHeight;

                return mapHeight; // better use .map surface height
            }

            return vmapHeight; // we have only vmapHeight (if have)
        }

        return mapHeight; // explicitly use map data
    }

    public float getWaterLevel(PhaseShift phaseShift, int mapId, float x, float y) {
        GridMap gmap = getGrid(PhasingHandler.getTerrainMapId(phaseShift, mapId, this, x, y), x, y);

        if (gmap != null)
            return gmap.getLiquidLevel(x, y);

        return 0;
    }

    public boolean isInWater(PhaseShift phaseShift, int mapId, Position pos, LiquidData data) {
        var zLiquidStatus = getLiquidStatus(phaseShift, mapId, pos, null, data, DEFAULT_COLLISION_HEIGHT);
        return zLiquidStatus.hasAnyFlag(ZLiquidStatus.IN_WATER, ZLiquidStatus.UNDER_WATER);
    }

    public boolean isUnderWater(PhaseShift phaseShift, int mapId, Position pos) {
        EnumFlag<ZLiquidStatus> zLiquidStatus = getLiquidStatus(phaseShift, mapId, pos,
                (byte) (LiquidHeaderTypeFlag.Water.getValue() | LiquidHeaderTypeFlag.Ocean.getValue()),
                null, DEFAULT_COLLISION_HEIGHT);
        return zLiquidStatus.hasFlag(ZLiquidStatus.UNDER_WATER);
    }

    public float getWaterOrGroundLevel(PhaseShift phaseShift, int mapId, Position pos, YieldResult<Float> ground, boolean swim, float collisionHeight, DynamicMapTree dynamicMapTree) {
        int terrainMapId = PhasingHandler.getTerrainMapId(phaseShift, mapId, this, pos.getX(), pos.getY());
        if (getGrid(terrainMapId, pos.getX(), pos.getY()) != null) {
            // we need ground level (including grid height version) for proper return water level in point
            float ground_z = getStaticHeight(phaseShift, mapId, pos.getX(), pos.getY(), pos.getZ() + Z_OFFSET_FIND_HEIGHT, true, 50.0f);


            if (dynamicMapTree != null)
                ground_z = Math.max(ground_z, dynamicMapTree.getHeight(new Vector3(pos.getX(), pos.getY(), pos.getZ() + Z_OFFSET_FIND_HEIGHT), 50.0f, phaseShift));

            if(ground != null) {
                ground.set(ground_z);
            }

            LiquidData liquid_status = new LiquidData();

            Position liquidPos = new Position(pos);
            liquidPos.setZ(ground_z);

            var res = getLiquidStatus(phaseShift, mapId, liquidPos, null, liquid_status, collisionHeight);
            Optional<ZLiquidStatus> element = res.toElement();
            if (element.isPresent()) {
                return switch (element.get()) {
                    case ABOVE_WATER -> Math.max(liquid_status.getLevel(), ground_z);
                    case NO_WATER -> ground_z;
                    default -> liquid_status.getLevel();
                };
            }

        }

        return MapDefine.VMAP_INVALID_HEIGHT_VALUE;
    }

}
