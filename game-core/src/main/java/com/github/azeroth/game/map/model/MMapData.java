package com.github.azeroth.game.map.model;

import com.badlogic.gdx.utils.IntMap;
import com.github.azeroth.common.Pair;
import lombok.Getter;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshQuery;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Getter
public class MMapData {
    public ConcurrentHashMap<Pair<Integer, Integer>, NavMeshQuery> navMeshQueries = new ConcurrentHashMap<>(); // instanceId to query

    private final ConcurrentHashMap<Integer, MMapMapData> meshData = new ConcurrentHashMap<>();

    public static int getInstanceIdForMeshLookup(int mapId, int instanceId) {
        return switch (mapId) {
            case 0, 1, 571, 603, 607, 609, 616, 628, 631, 644, 649, 720, 732, 754, 755, 861, 938, 940, 962, 967, 1064,
                 1076, 1098, 1122, 1126, 1182, 1205, 1220, 1265, 1492, 1523, 1530, 1579, 1676, 1704, 1705, 1706, 1707,
                 1734, 1756, 1943, 2076, 2118, 2160, 2161, 2187, 2212, 2235, 2237, 2264, 2450, 2512, 2586, 2601, 2654,
                 2657, 2660, 2669, 2819, 2828 -> instanceId;
            default -> 0;// for maps that won't have dynamic mesh, return 0 to reuse the same mesh across all instances
        };

    }


    public MMapMapData getMeshData(int mapId, int instanceId) {
        // for maps that won't have dynamic mesh, return 0 to reuse the same mesh across all instances
        return meshData.get(getInstanceIdForMeshLookup(mapId, instanceId));
    }


    public MMapMapData getMeshData(int mapId, int instanceId, Supplier<MMapMapData> supplier) {
        // for maps that won't have dynamic mesh, return 0 to reuse the same mesh across all instances
        return meshData.computeIfAbsent(getInstanceIdForMeshLookup(mapId, instanceId), k -> supplier.get());
    }
}
