package com.github.azeroth.game.chat;


import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.AnyUnitInObjectRangeCheck;
import com.github.azeroth.game.map.UnitListSearcher;
import com.github.azeroth.game.map.grid.Cell;
import com.github.azeroth.game.movement.PathGenerator;
import game.PhasingHandler;

import java.util.ArrayList;



class MMapsCommands {

    private static boolean handleMmapPathCommand(CommandHandler handler, StringArguments args) {
        if (global.getMMapMgr().getNavMesh(handler.getPlayer().getLocation().getMapId()) == null) {
            handler.sendSysMessage("NavMesh not loaded for current map.");

            return true;
        }

        handler.sendSysMessage("mmap path:");

        // units
        var player = handler.getPlayer();
        var target = handler.getSelectedUnit();

        if (player == null || target == null) {
            handler.sendSysMessage("Invalid target/source selection.");

            return true;
        }

        var para = args.NextString(" ");

        var useStraightPath = false;

        if (para.equalsIgnoreCase("true")) {
            useStraightPath = true;
        }

        var useRaycast = false;

        if (para.equalsIgnoreCase("line") || para.equalsIgnoreCase("ray") || para.equalsIgnoreCase("raycast")) {
            useRaycast = true;
        }

        // unit locations
        var pos = player.getLocation().Copy();

        // path
        PathGenerator path = new PathGenerator(target);
        path.setUseStraightPath(useStraightPath);
        path.setUseRaycast(useRaycast);
        var result = path.calculatePath(pos, false);

        var pointPath = path.getPath();
        handler.sendSysMessage("{0}'s path to {1}:", target.getName(), player.getName());
        handler.sendSysMessage("Building: {0}", useStraightPath ? "StraightPath" : useRaycast ? "Raycast" : "SmoothPath");
        handler.sendSysMessage("Result: {0} - Length: {1} - Type: {2}", (result ? "true" : "false"), pointPath.length, path.getPathType());

        var start = path.getStartPosition();
        var end = path.getEndPosition();
        var actualEnd = path.getActualEndPosition();

        handler.sendSysMessage("StartPosition     ({0:F3}, {1:F3}, {2:F3})", start.X, start.Y, start.Z);
        handler.sendSysMessage("EndPosition       ({0:F3}, {1:F3}, {2:F3})", end.X, end.Y, end.Z);
        handler.sendSysMessage("ActualEndPosition ({0:F3}, {1:F3}, {2:F3})", actualEnd.X, actualEnd.Y, actualEnd.Z);

        if (!player.isGameMaster()) {
            handler.sendSysMessage("Enable GM mode to see the path points.");
        }

        for (int i = 0; i < pointPath.length; ++i) {
            player.summonCreature(1, new Position(pointPath[i].X, pointPath[i].Y, pointPath[i].Z, 0), TempSummonType.TimedDespawn, duration.FromSeconds(9));
        }

        return true;
    }


    private static boolean handleMmapLocCommand(CommandHandler handler) {
        handler.sendSysMessage("mmap tileloc:");

        // grid tile location
        var player = handler.getPlayer();

        var gx = (int) (32 - player.getLocation().getX() / MapDefine.SizeofGrids);
        var gy = (int) (32 - player.getLocation().getY() / MapDefine.SizeofGrids);


        handler.sendSysMessage("{0:D4}{1:D2}{2:D2}.mmtile", player.getLocation().getMapId(), gy, gx);
        handler.sendSysMessage("tileloc [{0}, {1}]", gx, gy);

        // calculate navmesh tile location
        var terrainMapId = PhasingHandler.getTerrainMapId(player.getPhaseShift(), player.getLocation().getMapId(), player.getMap().getTerrain(), player.getLocation().getX(), player.getLocation().getY());
        var navmesh = global.getMMapMgr().getNavMesh(terrainMapId);
        var navmeshquery = global.getMMapMgr().getNavMeshQuery(terrainMapId, player.getInstanceId());

        if (navmesh == null || navmeshquery == null) {
            handler.sendSysMessage("NavMesh not loaded for current map.");

            return true;
        }

        var min = navmesh.getParams().orig;

        float[] location = {player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getX()};

        float[] extents = {3.0f, 5.0f, 3.0f};

        var tilex = (int) ((player.getLocation().getY() - min[0]) / MapDefine.SizeofGrids);
        var tiley = (int) ((player.getLocation().getX() - min[2]) / MapDefine.SizeofGrids);

        handler.sendSysMessage("Calc   [{0:D2}, {1:D2}]", tilex, tiley);

        // navmesh poly . navmesh tile location
        Detour.dtQueryFilter filter = new Detour.dtQueryFilter();
        var nothing = new float[3];
        long polyRef = 0;

        tangible.RefObject<Long> tempRef_polyRef = new tangible.RefObject<Long>(polyRef);
        tangible.RefObject<float[]> tempRef_nothing = new tangible.RefObject<float[]>(nothing);
        if (Detour.dtStatusFailed(navmeshquery.findNearestPoly(location, extents, filter, tempRef_polyRef, tempRef_nothing))) {
            nothing = tempRef_nothing.refArgValue;
            polyRef = tempRef_polyRef.refArgValue;
            handler.sendSysMessage("Dt     [??,??] (invalid poly, probably no tile loaded)");

            return true;
        } else {
            nothing = tempRef_nothing.refArgValue;
            polyRef = tempRef_polyRef.refArgValue;
        }

        if (polyRef == 0) {
            handler.sendSysMessage("Dt     [??, ??] (invalid poly, probably no tile loaded)");
        } else {
            Detour.dtMeshTile tile = new Detour.dtMeshTile();
            Detour.dtPoly poly = new Detour.dtPoly();

            tangible.RefObject<Detour.dtMeshTile> tempRef_tile = new tangible.RefObject<Detour.dtMeshTile>(tile);
            tangible.RefObject<Detour.dtPoly> tempRef_poly = new tangible.RefObject<Detour.dtPoly>(poly);
            if (Detour.dtStatusSucceed(navmesh.getTileAndPolyByRef(polyRef, tempRef_tile, tempRef_poly))) {
                poly = tempRef_poly.refArgValue;
                tile = tempRef_tile.refArgValue;
                if (tile != null) {
                    handler.sendSysMessage("Dt     [{0:D2},{1:D2}]", tile.header.x, tile.header.y);

                    return true;
                }
            } else {
                poly = tempRef_poly.refArgValue;
                tile = tempRef_tile.refArgValue;
            }

            handler.sendSysMessage("Dt     [??,??] (no tile loaded)");
        }

        return true;
    }


    private static boolean handleMmapLoadedTilesCommand(CommandHandler handler) {
        var player = handler.getSession().getPlayer();
        var terrainMapId = PhasingHandler.getTerrainMapId(player.getPhaseShift(), player.getLocation().getMapId(), player.getMap().getTerrain(), player.getLocation().getX(), player.getLocation().getY());
        var navmesh = global.getMMapMgr().getNavMesh(terrainMapId);
        var navmeshquery = global.getMMapMgr().getNavMeshQuery(terrainMapId, handler.getPlayer().getInstanceId());

        if (navmesh == null || navmeshquery == null) {
            handler.sendSysMessage("NavMesh not loaded for current map.");

            return true;
        }

        handler.sendSysMessage("mmap loadedtiles:");

        for (var i = 0; i < navmesh.getMaxTiles(); ++i) {
            var tile = navmesh.getTile(i);

            if (tile.header == null) {
                continue;
            }

            handler.sendSysMessage("[{0:D2}, {1:D2}]", tile.header.x, tile.header.y);
        }

        return true;
    }


    private static boolean handleMmapStatsCommand(CommandHandler handler) {
        var player = handler.getSession().getPlayer();
        var terrainMapId = PhasingHandler.getTerrainMapId(player.getPhaseShift(), player.getLocation().getMapId(), player.getMap().getTerrain(), player.getLocation().getX(), player.getLocation().getY());
        handler.sendSysMessage("mmap stats:");
        handler.sendSysMessage("  global mmap pathfinding is {0}abled", global.getDisableMgr().isPathfindingEnabled(player.getLocation().getMapId()) ? "En" : "Dis");
        handler.sendSysMessage(" {0} maps loaded with {1} tiles overall", global.getMMapMgr().getLoadedMapsCount(), global.getMMapMgr().getLoadedTilesCount());

        var navmesh = global.getMMapMgr().getNavMesh(terrainMapId);

        if (navmesh == null) {
            handler.sendSysMessage("NavMesh not loaded for current map.");

            return true;
        }

        int tileCount = 0;
        var nodeCount = 0;
        var polyCount = 0;
        var vertCount = 0;
        var triCount = 0;
        var triVertCount = 0;

        for (var i = 0; i < navmesh.getMaxTiles(); ++i) {
            var tile = navmesh.getTile(i);

            if (tile.header == null) {
                continue;
            }

            tileCount++;
            nodeCount += tile.header.bvNodeCount;
            polyCount += tile.header.polyCount;
            vertCount += tile.header.vertCount;
            triCount += tile.header.detailTriCount;
            triVertCount += tile.header.detailVertCount;
        }

        handler.sendSysMessage("Navmesh stats:");
        handler.sendSysMessage(" {0} tiles loaded", tileCount);
        handler.sendSysMessage(" {0} BVTree nodes", nodeCount);
        handler.sendSysMessage(" {0} polygons ({1} vertices)", polyCount, vertCount);
        handler.sendSysMessage(" {0} triangles ({1} vertices)", triCount, triVertCount);

        return true;
    }


    private static boolean handleMmapTestArea(CommandHandler handler) {
        var radius = 40.0f;
        WorldObject obj = handler.getPlayer();

        // Get Creatures
        ArrayList<Unit> creatureList = new ArrayList<>();

        var go_check = new AnyUnitInObjectRangeCheck(obj, radius);
        var go_search = new UnitListSearcher(obj, creatureList, go_check, gridType.Grid);

        Cell.visitGrid(obj, go_search, radius);

        if (!creatureList.isEmpty()) {
            handler.sendSysMessage("Found {0} Creatures.", creatureList.size());

            int paths = 0;
            var uStartTime = System.currentTimeMillis();

            for (var creature : creatureList) {
                PathGenerator path = new PathGenerator(creature);
                path.calculatePath(obj.getLocation());
                ++paths;
            }

            var uPathLoadTime = time.GetMSTimeDiffToNow(uStartTime);
            handler.sendSysMessage("Generated {0} paths in {1} ms", paths, uPathLoadTime);
        } else {
            handler.sendSysMessage("No creatures in {0} yard range.", radius);
        }

        return true;
    }
}
