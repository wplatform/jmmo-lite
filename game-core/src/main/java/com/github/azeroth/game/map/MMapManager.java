package com.github.azeroth.game.map;


import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntMap;
import com.github.azeroth.common.Logs;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.domain.map.MMapData;
import com.github.azeroth.game.domain.map.MmapTileHeader;
import org.recast4j.detour.MeshTile;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.io.MeshDataReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class MMapManager {
    private static final String MAP_FILE_NAME_FORMAT = "mmaps/%4d.mmap";
    private static final String TILE_FILE_NAME_FORMAT = "mmaps/%4d%2d%2d.mmtile";

    private final IntMap<MMapData> loadedMMaps = new IntMap<>();
    private final IntIntMap parentMapData = new IntIntMap();
    private int loadedTiles;
    private Path mMapPath;

    private MMapManager() {
    }

    public final void initialize(IntMap<IntArray> mapData) {
        for (var pair : mapData) {
            parentMapData.put(pair.value, pair.key);
        }
    }

    public final boolean loadMap(int mapId, int x, int y) {
        // make sure the mmap is loaded and ready to load tiles
        if (!loadMapData(mapId)) {
            return false;
        }

        // get this mmap data
        var mmap = loadedMMaps.get(mapId);

        // check if we already have this tile loaded
        var packedGridPos = packTileID(x, y);

        if (mmap.loadedTileRefs.containsKey(packedGridPos)) {
            return false;
        }

        // load this tile . mmaps/MMMMXXYY.mmtile
        Path filePath = mMapPath.resolve(TILE_FILE_NAME_FORMAT.formatted(mapId, x, y));


        if (!Files.exists(filePath) && parentMapData.containsKey(mapId)) {
            filePath = mMapPath.resolve(TILE_FILE_NAME_FORMAT.formatted(parentMapData.get(mapId, -1), x, y));
        }

        if (!Files.exists(filePath)) {

            Logs.MAPS.error("MMAP:loadMap: Could not open mmtile file '{}'", filePath.toAbsolutePath());


            return false;
        }

        try (FileChannel fileChannel = FileChannel.open(filePath)) {
            ByteBuffer buffer = ByteBuffer.allocate((int) fileChannel.size());
            fileChannel.read(buffer);
            buffer.flip();

            // read header
            MmapTileHeader fileHeader = new MmapTileHeader(buffer);

            if (fileHeader.mmapMagic() != MapDefine.MMAP_MAGIC) {
                Logs.MAPS.error("MMAP:loadMap: Bad header in mmap {}", filePath);

                return false;
            }

            if (fileHeader.mmapVersion() != MapDefine.MMAP_VERSION) {
                Logs.MAPS.error("MMAP:loadMap: {} was built with generator v{}, expected v{}",
                        filePath, fileHeader.mmapVersion(), MapDefine.MMAP_VERSION);
                return false;
            }

            if (buffer.limit() != fileChannel.size()) {
                Logs.MAPS.error("MMAP:loadMap: {} has corrupted data size", filePath);
            }

            MeshTile meshTile = new MeshTile(packedGridPos);
            MeshDataReader meshDataReader = new MeshDataReader();
            meshTile.data = meshDataReader.read(buffer, 0);

            mmap.loadedTileRefs.put(packedGridPos)


            Detour.dtRawTileData data = new Detour.dtRawTileData();
            data.FromBytes(bytes, 0);

            long tileRef = 0;

            // memory allocated for data is now managed by detour, and will be deallocated when the tile is removed
            tangible.RefObject<Long> tempRef_tileRef = new tangible.RefObject<Long>(tileRef);
            if (Detour.dtStatusSucceed(mmap.navMesh.addTile(data, 1, 0, tempRef_tileRef))) {
                tileRef = tempRef_tileRef.refArgValue;
                mmap.loadedTileRefs.put(packedGridPos, tileRef);
                ++loadedTiles;
                Log.outInfo(LogFilter.Maps, "MMAP:loadMap: Loaded mmtile {0:D4}[{1:D2}, {2:D2}]", mapId, x, y);

                return true;
            }

            Logs.MAPS.error("MMAP:loadMap: Could not load {0:D4}{1:D2}{2:D2}.mmtile into navmesh", mapId, x, y);

            return false;
        } catch (IOException e) {
            Logs.MAPS.error("MMAP:loadMap: Could not load {} into navmesh", filePath, e);
            return false;
        }

    }

    public final boolean loadMapInstance(int mapId, int instanceId) {
        if (!loadMapData(mapId)) {
            return false;
        }

        var mmap = loadedMMaps.get(mapId);

        if (mmap.navMeshQueries.containsKey(instanceId)) {
            return true;
        }

        // allocate mesh query
        NavMeshQuery query = new NavMeshQuery(mmap.navMesh);


        Logs.MAPS.info("MMAP:GetNavMeshQuery: created dtNavMeshQuery for mapId {:04} instanceId {}", instanceMapId, instanceId);
        mmap.navMeshQueries.put(instanceId, query);

        return true;
    }

    public final boolean unloadMap(int mapId, int x, int y) {
        // check if we have this map loaded
        TValue mmap;
        if (!(loadedMMaps.containsKey(mapId) && (mmap = loadedMMaps.get(mapId)) == mmap)) {
            return false;
        }

        // check if we have this tile loaded
        var packedGridPos = packTileID(x, y);

        var tileRef;

        if (!mmap.loadedTileRefs.TryGetValue(packedGridPos, out tileRef)) {
            return false;
        }

        // unload, and mark as non loaded

        if (!Detour.dtStatusFailed(mmap.navMesh.removeTile(tileRef, out _))) {
            mmap.loadedTileRefs.remove(packedGridPos);
            --loadedTiles;
            Log.outInfo(LogFilter.Maps, "MMAP:unloadMap: Unloaded mmtile {0:D4}[{1:D2}, {2:D2}] from {3:D4}", mapId, x, y, mapId);

            return true;
        }

        return false;
    }

    public final boolean unloadMap(int mapId) {
        if (!loadedMMaps.containsKey(mapId)) {
            // file may not exist, therefore not loaded
            Logs.MAPS.debug("MMAP:unloadMap: Asked to unload not loaded navmesh map {0:D4}", mapId);

            return false;
        }

        // unload all tiles from given map
        var mmap = loadedMMaps.get(mapId);

        for (var i : mmap.loadedTileRefs) {
            var x = (i.key >> 16);
            var y = (i.key & 0x0000FFFF);


            if (Detour.dtStatusFailed(mmap.navMesh.removeTile(i.value, out _))) {
                Logs.MAPS.error("MMAP:unloadMap: Could not unload {0:D4}{1:D2}{2:D2}.mmtile from navmesh", mapId, x, y);
            } else {
                --loadedTiles;
                Log.outInfo(LogFilter.Maps, "MMAP:unloadMap: Unloaded mmtile {0:D4} [{1:D2}, {2:D2}] from {3:D4}", mapId, x, y, mapId);
            }
        }

        loadedMMaps.remove(mapId);
        Log.outInfo(LogFilter.Maps, "MMAP:unloadMap: Unloaded {0:D4}.mmap", mapId);

        return true;
    }

    public final boolean unloadMapInstance(int mapId, int instanceId) {
        // check if we have this map loaded
        TValue mmap;
        if (!(loadedMMaps.containsKey(mapId) && (mmap = loadedMMaps.get(mapId)) == mmap)) {
            // file may not exist, therefore not loaded
            Logs.MAPS.debug("MMAP:unloadMapInstance: Asked to unload not loaded navmesh map {0}", mapId);

            return false;
        }

        if (!mmap.navMeshQueries.ContainsKey(instanceId)) {
            Logs.MAPS.debug("MMAP:unloadMapInstance: Asked to unload not loaded dtNavMeshQuery mapId {0} instanceId {1}", mapId, instanceId);

            return false;
        }

        mmap.navMeshQueries.remove(instanceId);
        Log.outInfo(LogFilter.Maps, "MMAP:unloadMapInstance: Unloaded mapId {0} instanceId {1}", mapId, instanceId);

        return true;
    }

    public final Detour.dtNavMesh getNavMesh(int mapId) {
        TValue mmap;
        if (!(loadedMMaps.containsKey(mapId) && (mmap = loadedMMaps.get(mapId)) == mmap)) {
            return null;
        }

        return mmap.navMesh;
    }

    public final Detour.dtNavMeshQuery getNavMeshQuery(int mapId, int instanceId) {
        TValue mmap;
        if (!(loadedMMaps.containsKey(mapId) && (mmap = loadedMMaps.get(mapId)) == mmap)) {
            return null;
        }

        return mmap.navMeshQueries.get(instanceId);
    }

    public final int getLoadedTilesCount() {
        return loadedTiles;
    }

    public final int getLoadedMapsCount() {
        return loadedMMaps.size();
    }

    private MMapData getMMapData(int mapId) {
        return loadedMMaps.get(mapId);
    }

    private boolean loadMapData(int mapId) {
        // we already have this map loaded?
        TValue mmap;
        if ((loadedMMaps.containsKey(mapId) && (mmap = loadedMMaps.get(mapId)) == mmap) && mmap != null) {
            return true;
        }

        // load and init dtNavMesh - read parameters from file
        var filename = String.format(MAP_FILE_NAME_FORMAT, basePath, mapId);

        if (!(new file(filename)).isFile()) {
            Logs.MAPS.error("Could not open mmap file {0}", filename);

            return false;
        }

        try (BinaryReader reader = new BinaryReader(new FileInputStream(filename), Encoding.UTF8)) {
            Detour.dtNavMeshParams params = new Detour.dtNavMeshParams();
            params.orig[0] = reader.ReadSingle();
            params.orig[1] = reader.ReadSingle();
            params.orig[2] = reader.ReadSingle();

            params.tileWidth = reader.ReadSingle();
            params.tileHeight = reader.ReadSingle();
            params.maxTiles = reader.readInt32();
            params.maxPolys = reader.readInt32();

            Detour.dtNavMesh mesh = new Detour.dtNavMesh();

            if (Detour.dtStatusFailed(mesh.init(params))) {
                Logs.MAPS.error("MMAP:loadMapData: Failed to initialize dtNavMesh for mmap {0:D4} from file {1}", mapId, filename);

                return false;
            }

            Log.outInfo(LogFilter.Maps, "MMAP:loadMapData: Loaded {0:D4}.mmap", mapId);

            // store inside our map list
            loadedMMaps.put(mapId, new MMapData(mesh));

            return true;
        }
    }

    private int packTileID(int x, int y) {
        return (int) (x << 16 | y);
    }
}
