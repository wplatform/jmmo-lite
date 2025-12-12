package com.github.azeroth.game.map.model;

import org.recast4j.detour.NavMesh;

import java.util.HashMap;

public class MMapMapData {

    public NavMesh navMesh;
    public HashMap<Integer, Long> loadedTileRefs = new HashMap<>(); // maps [map grid coords] to [dtTile]


}
