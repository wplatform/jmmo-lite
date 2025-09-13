package com.github.azeroth.game.pools;


import com.badlogic.gdx.utils.IntArray;
import com.github.azeroth.game.map.Map;
import com.github.azeroth.game.domain.spawn.SpawnObjectType;

import java.util.HashMap;

public class SpawnedPoolData {
    private final Map owner;

    private final IntArray spawnedCreatures = new IntArray();

    private final IntArray spawnedGameobjects = new IntArray();

    private final HashMap<Long, Integer> spawnedPools = new HashMap<Long, Integer>();

    public SpawnedPoolData(Map owner) {
        this.owner = owner;
    }

    public final Map getMap() {
        return owner;
    }


    public final int getSpawnedObjects(int poolId) {
        return spawnedPools.get(poolId);
    }


    public final <T> boolean isSpawnedObject(long dbGuid) {
        switch (T.class.name) {
            case "Creature":
                return spawnedCreatures.contains(dbGuid);
            case "GameObject":
                return spawnedGameObjects.contains(dbGuid);
            case "Pool":
                return spawnedPools.containsKey(dbGuid);
            default:
                return false;
        }
    }


    public final boolean isSpawnedObject(SpawnObjectType type, long dbGuidOrPoolId) {
        switch (type) {
            case CREATURE:
                return spawnedCreatures.contains(dbGuidOrPoolId);
            case GAME_OBJECT:
                return spawnedGameObjects.contains(dbGuidOrPoolId);
            default:
                Log.outFatal(LogFilter.misc, String.format("Invalid spawn type %1$s passed to SpawnedPoolData::IsSpawnedObject (with spawnId %2$s)", type, dbGuidOrPoolId));

                return false;
        }
    }


    public final <T> void addSpawn(long dbGuid, int poolId) {
        switch (T.class.name) {
            case "Creature":
                spawnedCreatures.add(dbGuid);

                break;
            case "GameObject":
                spawnedGameObjects.add(dbGuid);

                break;
            case "Pool":
                spawnedPools.put(dbGuid, 0);

                break;
            default:
                return;
        }

        if (!spawnedPools.containsKey(poolId)) {
            spawnedPools.put(poolId, 0);
        }

        ++spawnedPools.get(poolId);
    }


    public final <T> void removeSpawn(long dbGuid, int poolId) {
        switch (T.class.name) {
            case "Creature":
                spawnedCreatures.remove((Long) dbGuid);

                break;
            case "GameObject":
                spawnedGameObjects.remove((Long) dbGuid);

                break;
            case "Pool":
                spawnedPools.remove(dbGuid);

                break;
            default:
                return;
        }

        if (spawnedPools.get(poolId).compareTo(0) > 0) {
            --_spawnedPools.get(poolId);
        }
    }
}
