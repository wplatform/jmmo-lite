package com.github.azeroth.game.map;



import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.object.WorldLocation;
import com.github.azeroth.game.domain.map.ZoneAndAreaId;
import com.github.azeroth.game.domain.phasing.PhaseShift;


import java.util.HashMap;
import java.util.HashSet;

public class TerrainManager {

    private final HashMap<Integer, TerrainInfo> terrainMaps = new HashMap<Integer, TerrainInfo>();

    private final HashSet<Integer> keepLoaded = new HashSet<Integer>();

    private final limitedThreadTaskManager threadTaskManager = new limitedThreadTaskManager(ConfigMgr.GetDefaultValue("Map.ParellelUpdateTasks", 20));

    // parent map links
    private MultiMap<Integer, Integer> parentMapData = new MultiMap<Integer, Integer>();

    private TerrainManager() {
    }


    public static boolean existMapAndVMap(int mapid, float x, float y) {
        var p = MapDefine.computeGridCoord(x, y);

        var gx = (int) ((MapDefine.MaxGrids - 1) - p.getXCoord());
        var gy = (int) ((MapDefine.MaxGrids - 1) - p.getYCoord());

        return TerrainInfo.existMap(mapid, gx, gy) && TerrainInfo.existVMap(mapid, gx, gy);
    }


    public final void initializeParentMapData(MultiMap<Integer, Integer> mapData) {
        parentMapData = mapData;

        var result = DB.World.query("SELECT mapid FROM map_keeploaded");

        if (!result.isEmpty()) {
            do {
                keepLoaded.add(result.<Integer>Read(0));
            } while (result.NextRow());
        }
    }


    public final TerrainInfo loadTerrain(int mapId) {
        var entry = dbc.MapStorage.get(mapId);

        if (entry == null) {
            return null;
        }

        while (entry.ParentMapID != -1 || entry.CosmeticParentMapID != -1) {
            var parentMapId = (int) (entry.ParentMapID != -1 ? entry.ParentMapID : entry.CosmeticParentMapID);
            entry = CliDB.MapStorage.get(parentMapId);

            if (entry == null) {
                break;
            }

            mapId = parentMapId;
        }

        var terrain = terrainMaps.get(mapId);

        if (terrain != null) {
            return terrain;
        }

        var terrainInfo = loadTerrainImpl(mapId);
        terrainMaps.put(mapId, terrainInfo);

        return terrainInfo;
    }

    public final void unloadAll() {
        terrainMaps.clear();
    }


    public final void update(int diff) {
        // global garbage collection

        for (var(mapId, terrain) : terrainMaps) {
            threadTaskManager.Schedule(() ->
            {
                if (terrain != null) {
                    terrain.cleanUpGrids(diff);
                }
            });
        }

        threadTaskManager.Wait();
    }


    public final int getAreaId(PhaseShift phaseShift, int mapid, Position pos) {
        return getAreaId(phaseShift, mapid, pos.getX(), pos.getY(), pos.getZ());
    }


    public final int getAreaId(PhaseShift phaseShift, WorldLocation loc) {
        return getAreaId(phaseShift, loc.getMapId(), loc);
    }


    public final int getAreaId(PhaseShift phaseShift, int mapid, float x, float y, float z) {
        var terrain = loadTerrain(mapid);

        if (terrain != null) {
            return terrain.getAreaId(phaseShift, mapid, x, y, z);
        }

        return 0;
    }


    public final int getZoneId(PhaseShift phaseShift, int mapid, Position pos) {
        return getZoneId(phaseShift, mapid, pos.getX(), pos.getY(), pos.getZ());
    }


    public final int getZoneId(PhaseShift phaseShift, WorldLocation loc) {
        return getZoneId(phaseShift, loc.getMapId(), loc);
    }


    public final int getZoneId(PhaseShift phaseShift, int mapId, float x, float y, float z) {
        var terrain = loadTerrain(mapId);

        if (terrain != null) {
            return terrain.getZoneId(phaseShift, mapId, x, y, z);
        }

        return 0;
    }


    public final ZoneAndAreaId getZoneAndAreaId(PhaseShift phaseShift, int mapId, Position pos) {
        return getZoneAndAreaId(phaseShift, mapId, pos.getX(), pos.getY(), pos.getZ());
    }


    public final ZoneAndAreaId getZoneAndAreaId(PhaseShift phaseShift, WorldLocation loc) {
        return getZoneAndAreaId(phaseShift, loc.getMapId(), loc);
    }


    public final ZoneAndAreaId getZoneAndAreaId(PhaseShift phaseShift, int mapId, float x, float y, float z) {

        var terrain = loadTerrain(mapId);

        if (terrain != null) {
            return terrain.getZoneAndAreaId(phaseShift, mapId, x, y, z, null);
        }
        return null;
    }


    public final boolean keepMapLoaded(int mapid) {
        return keepLoaded.contains(mapid);
    }


    private TerrainInfo loadTerrainImpl(int mapId) {
        var rootTerrain = new TerrainInfo(mapId, keepLoaded.contains(mapId));

        rootTerrain.discoverGridMapFiles();

        for (var childMapId : parentMapData.get(mapId)) {
            rootTerrain.addChildTerrain(loadTerrainImpl(childMapId));
        }

        return rootTerrain;
    }
}
