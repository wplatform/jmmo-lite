package com.github.azeroth.game.map;


import com.badlogic.gdx.utils.IntIntMap;
import com.github.azeroth.common.Assert;
import com.github.azeroth.common.Logs;
import com.github.azeroth.common.Pair;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.domain.map.MmapTileHeader;
import com.github.azeroth.game.domain.map.enums.LoadResult;
import com.github.azeroth.game.map.model.MMapData;
import com.github.azeroth.game.map.model.MMapMapData;
import com.github.azeroth.utils.StringUtil;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshParams;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.io.MeshDataReader;
import org.recast4j.detour.io.NavMeshParamReader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.azeroth.game.domain.map.MapDefine.MMAP_MAGIC;
import static com.github.azeroth.game.domain.map.MapDefine.MMAP_VERSION;

public class MMapManager {
    private static final String MAP_FILE_NAME_FORMAT = "mmaps/{:04}.mmap";
    private static final String TILE_FILE_NAME_FORMAT = "mmaps/{:04}_{:02}_{:02}.mmtile";

    private final HashMap<Integer, MMapData> loadedMMaps = new HashMap<>();

    private final HashMap<Pair<Integer, Integer>, NavMeshQuery> navMeshQueries = new HashMap<>();
    private final IntIntMap parentMapData = new IntIntMap();
    private AtomicInteger loadedTiles = new AtomicInteger(0);
    private Path dataPath;

    private MMapManager() {
    }

    public final void initialize(IntIntMap mapData) {
        for (var pair : mapData) {
            parentMapData.put(pair.value, pair.key);
        }
    }

    public final LoadResult loadMap(int mapId, int instanceId, int x, int y) {
        // make sure the mmap is loaded and ready to load tiles
        LoadResult loadResult = loadMapData(mapId, instanceId);
        switch (loadResult) {
            case Success:
            case AlreadyLoaded:
                break;
            default:
                return loadResult;
        }

        // get this mmap data
        var mmap = loadedMMaps.get(mapId);
        var meshData = mmap.getMeshData(mapId, instanceId);


        // check if we already have this tile loaded
        var packedGridPos = packTileID(x, y);
        if (meshData.loadedTileRefs.containsKey(packedGridPos))
            return LoadResult.AlreadyLoaded;


        // load this tile . mmaps/MMMMXXYY.mmtile
        Path filePath = dataPath.resolve(StringUtil.format(TILE_FILE_NAME_FORMAT, mapId, x, y));


        if (!Files.exists(filePath) && parentMapData.containsKey(mapId)) {
            filePath = dataPath.resolve(StringUtil.format(TILE_FILE_NAME_FORMAT, parentMapData.get(mapId, -1), x, y));
        }

        if (!Files.exists(filePath)) {
            Logs.MAPS.error("MMAP:loadMap: Could not open mmtile file '{}'", filePath.toAbsolutePath());
            return LoadResult.FileNotFound;
        }

        try (FileChannel fileChannel = FileChannel.open(filePath)) {
            ByteBuffer buffer = ByteBuffer.allocate((int) fileChannel.size());
            fileChannel.read(buffer);
            buffer.flip();

            // read header
            MmapTileHeader fileHeader = new MmapTileHeader(buffer);
            if (fileHeader.mmapMagic() != MapDefine.MMAP_MAGIC) {
                Logs.MAPS.error("MMAP:loadMap: Bad header in mmap {:04}_{:02}_{:02}.mmtile", mapId, x, y);
                return LoadResult.ReadFromFileFailed;
            }

            if (fileHeader.mmapVersion() != MapDefine.MMAP_VERSION) {
                Logs.MAPS.error("MMAP:loadMap: {:04}_{:02}_{:02}.mmtile was built with generator v{}, expected v{}",
                        mapId, x, y, fileHeader.mmapVersion(), MMAP_VERSION);
                return LoadResult.VersionMismatch;
            }

            if (buffer.limit() != fileChannel.size()) {
                Logs.MAPS.error("MMAP:loadMap: {:04}_{:02}_{:02}.mmtile has corrupted data size", mapId, x, y);
                return LoadResult.ReadFromFileFailed;
            }


            long tileRef = meshData.navMesh.addTile(new MeshDataReader().read(buffer, 0), 0x01, 0);
            meshData.loadedTileRefs.put(packedGridPos, tileRef);


            Logs.MAPS.error("MMAP:loadMap: Could not load {0:D4}{1:D2}{2:D2}.mmtile into navmesh", mapId, x, y);

            return LoadResult.Success;
        } catch (IOException e) {
            Logs.MAPS.error("MMAP:loadMap: Could not load {} into navmesh", filePath, e);
            return LoadResult.ReadFromFileFailed;
        }

    }

    public final boolean loadMapInstance(int meshMapId, int instanceMapId, int instanceId) {
        var loadResult = loadMapData(meshMapId, instanceId);
        switch (loadResult) {
            case Success:
            case AlreadyLoaded:
                break;
            default:
                return false;
        }

        var mmap = loadedMMaps.get(meshMapId);
        var navMeshQuery = mmap.navMeshQueries.computeIfAbsent(Pair.of(instanceMapId, instanceId), k -> {
            Logs.MAPS.debug("MMAP:GetNavMeshQuery: created dtNavMeshQuery for mapId {:04} instanceId {}", instanceMapId, instanceId);
            MMapMapData meshData = mmap.getMeshData(instanceMapId, instanceId);
            return meshData == null ? null : new NavMeshQuery(meshData.navMesh);
        });

        return navMeshQuery != null;
    }

    public final void unloadMap(int mapId, int x, int y) {
        // check if we have this map loaded
        MMapData mMapData = getMMapData(mapId);
        if (mMapData == null) {
            // file may not exist, therefore not loaded
            Logs.MAPS.debug("MMAP:unloadMap: Asked to unload not loaded navmesh map. {:04}_{:02}_{:02}.mmtile", mapId, x, y);
            return;
        }

        // check if we have this tile loaded
        var packedGridPos = packTileID(x, y);

        mMapData.getMeshData().forEach((instanceId, meshData) -> {
            // check if we have this tile loaded
            Long tileRef = meshData.loadedTileRefs.get(packedGridPos);
            if (tileRef == null)
                return;
            long removeTile = meshData.navMesh.removeTile(tileRef);
            meshData.loadedTileRefs.put(packedGridPos, removeTile);
            loadedTiles.decrementAndGet();
            Logs.MAPS.debug("MMAP:unloadMap: Unloaded mmtile {:04}[{:02}, {:02}] from {:03}", mapId, x, y, mapId);

        });

    }

    public final boolean unloadMap(int mapId) {
        MMapData mMapData = loadedMMaps.get(mapId);
        if (mMapData == null) {
            // file may not exist, therefore not loaded
            Logs.MAPS.debug("MMAP:unloadMap: Asked to unload not loaded navmesh map {0:D4}", mapId);

            return false;
        }

        if (MMapData.getInstanceIdForMeshLookup(mapId, Integer.MAX_VALUE) == 0) {
            // unload all tiles from given map
            MMapMapData mesh = mMapData.getMeshData().get(0);
            for (var entry : mesh.loadedTileRefs.entrySet()) {
                int tileId = entry.getKey();
                long tileRef = entry.getValue();
                int x = (tileId >> 16);
                int y = (tileId & 0x0000FFFF);
                long removeTile = mesh.navMesh.removeTile(tileRef);
                entry.setValue(removeTile);
                loadedTiles.decrementAndGet();
                Logs.MAPS.debug("MMAP:unloadMap: Unloaded mmtile {:04}[{:02}, {:02}] from {:04}", mapId, x, y, mapId);
            }
        } else {
            // require all tiles to be already unloaded
            mMapData.getMeshData().forEach((instanceId, meshData) ->
                    Assert.isTrue(meshData.loadedTileRefs.isEmpty(), "MMAP:unloadMap: unload navmesh map {0:D4} but some tiles are still loaded", instanceId));
        }

        Logs.MAPS.debug("MMAP:unloadMap: Unloaded {:04}.mmap", mapId);
        return true;
    }

    public final boolean unloadMapInstance(int mapId, int instanceMapId, int instanceId) {
        // check if we have this map loaded
        MMapData mMapData = loadedMMaps.get(mapId);
        if (mMapData == null) {
            // file may not exist, therefore not loaded
            Logs.MAPS.debug("MMAP:unloadMapInstance: Asked to unload not loaded navmesh map {:04}", mapId);

            return false;
        }

        NavMeshQuery navMeshQuery = mMapData.navMeshQueries.remove(Pair.of(instanceMapId, instanceId));
        if (navMeshQuery == null) {
            Logs.MAPS.debug("MMAP:unloadMapInstance: Asked to unload not loaded NavMeshQuery mapId {:04} instanceId {}", instanceMapId, instanceId);

            return false;
        }

        MMapMapData meshData = mMapData.getMeshData(instanceMapId, instanceId);
        if (meshData != null) {
            Logs.MAPS.debug("MMAP:unloadMapInstance: Asked to unload not loaded NavMeshQuery mapId {:04} instanceId {}", instanceMapId, instanceId);

            return false;
        }

        return true;
    }

    public final NavMesh getNavMesh(int mapId, int instanceId) {
        MMapData mMapData = loadedMMaps.get(mapId);
        if (mMapData == null) {
            return null;
        }
        MMapMapData meshData = mMapData.getMeshData(mapId, instanceId);
        if (meshData == null) {
            return null;
        }
        return meshData.navMesh;
    }

    public final NavMeshQuery getNavMeshQuery(int mapId, int instanceMapId, int instanceId) {
        MMapData mMapData = loadedMMaps.get(mapId);
        if (mMapData == null) {
            return null;
        }
        return mMapData.navMeshQueries.get(Pair.of(instanceMapId, instanceId));
    }

    public final int getLoadedTilesCount() {
        return loadedTiles.get();
    }

    public final int getLoadedMapsCount() {
        return loadedMMaps.size();
    }

    private MMapData getMMapData(int mapId) {
        return loadedMMaps.get(mapId);
    }

    private LoadResult loadMapData(int mapId, int instanceId) {
        MMapData mMapData = loadedMMaps.computeIfAbsent(mapId, k -> new MMapData());
        var meshData = mMapData.getMeshData(mapId, instanceId);

        if (meshData != null) {
            return LoadResult.AlreadyLoaded;
        }
        meshData = new MMapMapData();

        Path fileName = this.dataPath.resolve(StringUtil.format(MAP_FILE_NAME_FORMAT, mapId));

        // Not return error if file not found
        if (!Files.exists(fileName)) {
            Logs.MAPS.error("MMAP:parseNavMeshParamsFile: Error: Could not open mmap file '{}'", fileName);
            return LoadResult.FileNotFound;
        }

        try (FileChannel fileChannel = FileChannel.open(fileName, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(36);
            fileChannel.read(buffer);
            buffer.flip();

            int mmapMagic = buffer.getInt();
            int mmapVersion = buffer.getInt();
            NavMeshParams params = new NavMeshParamReader().read(buffer);


            if (mmapMagic != MMAP_MAGIC) {
                Logs.MAPS.error("MMAP:loadMap: Bad header in mmap {:04}.mmap", mapId);
                return LoadResult.VersionMismatch;
            }

            if (mmapVersion != MMAP_VERSION) {
                Logs.MAPS.error("MMAP:loadMap: {:04}.mmap was built with generator v{}, expected v{}",
                        mapId, mmapVersion, MMAP_VERSION);
                return LoadResult.VersionMismatch;
            }

            meshData.navMesh = new NavMesh(params, 6);//DT_VERTS_PER_POLYGON

        } catch (IOException e) {
            Logs.MAPS.error("Error loading map file {} error.", fileName.toAbsolutePath(), e);
        }
        return LoadResult.ReadFromFileFailed;
    }


    private int packTileID(int x, int y) {
        return x << 16 | y;
    }
}
