package com.github.azeroth.game.pools;


import java.util.ArrayList;
import java.util.Objects;

public class PoolGroup<T> {
    private final ArrayList<PoolObject> explicitlyChanced = new ArrayList<>();
    private final ArrayList<PoolObject> equalChanced = new ArrayList<>();


    private int poolId;

    public PoolGroup() {
        poolId = 0;
    }

    public final boolean isEmptyDeepCheck() {
        if (!Objects.equals(T.class.name, "Pool")) {
            return isEmpty();
        }

        for (var explicitlyChanced : explicitlyChanced) {
            if (!global.getPoolMgr().isEmpty((int) explicitlyChanced.guid)) {
                return false;
            }
        }

        for (var equalChanced : equalChanced) {
            if (!global.getPoolMgr().isEmpty((int) equalChanced.guid)) {
                return false;
            }
        }

        return true;
    }


    public final void addEntry(PoolObject poolitem, int maxentries) {
        if (poolitem.chance != 0 && maxentries == 1) {
            explicitlyChanced.add(poolitem);
        } else {
            equalChanced.add(poolitem);
        }
    }

    public final boolean checkPool() {
        if (equalChanced.isEmpty()) {
            float chance = 0;

            for (var i = 0; i < explicitlyChanced.size(); ++i) {
                chance += explicitlyChanced.get(i).chance;
            }

            if (chance != 100 && chance != 0) {
                return false;
            }
        }

        return true;
    }


    public final void despawnObject(SpawnedPoolData spawns, long guid) {
        despawnObject(spawns, guid, false);
    }

    public final void despawnObject(SpawnedPoolData spawns) {
        despawnObject(spawns, 0, false);
    }

    public final void despawnObject(SpawnedPoolData spawns, long guid, boolean alwaysDeleteRespawnTime) {
        for (var i = 0; i < equalChanced.size(); ++i) {
            // if spawned
            if (spawns.<T>IsSpawnedObject(equalChanced.get(i).guid)) {
                if (guid == 0 || equalChanced.get(i).guid == guid) {
                    despawn1Object(spawns, equalChanced.get(i).guid, alwaysDeleteRespawnTime);
                    spawns.<T>RemoveSpawn(equalChanced.get(i).guid, poolId);
                }
            } else if (alwaysDeleteRespawnTime) {
                removeRespawnTimeFromDB(spawns, equalChanced.get(i).guid);
            }
        }

        for (var i = 0; i < explicitlyChanced.size(); ++i) {
            // spawned
            if (spawns.<T>IsSpawnedObject(explicitlyChanced.get(i).guid)) {
                if (guid == 0 || explicitlyChanced.get(i).guid == guid) {
                    despawn1Object(spawns, explicitlyChanced.get(i).guid, alwaysDeleteRespawnTime);
                    spawns.<T>RemoveSpawn(explicitlyChanced.get(i).guid, poolId);
                }
            } else if (alwaysDeleteRespawnTime) {
                removeRespawnTimeFromDB(spawns, explicitlyChanced.get(i).guid);
            }
        }
    }


    public final void removeOneRelation(int child_pool_id) {
        if (!Objects.equals(T.class.name, "Pool")) {
            return;
        }

        for (var poolObject : explicitlyChanced) {
            if (poolObject.guid == child_pool_id) {
                explicitlyChanced.remove(poolObject);

                break;
            }
        }

        for (var poolObject : equalChanced) {
            if (poolObject.guid == child_pool_id) {
                equalChanced.remove(poolObject);

                break;
            }
        }
    }


    public final void spawnObject(SpawnedPoolData spawns, int limit, long triggerFrom) {
        var count = (int) (limit - spawns.getSpawnedObjects(poolId));

        // If triggered from some object respawn this object is still marked as spawned
        // and also counted into m_SpawnedPoolAmount so we need increase count to be
        // spawned by 1
        if (triggerFrom != 0) {
            ++count;
        }

        // This will try to spawn the rest of pool, not guaranteed
        if (count > 0) {
            ArrayList<PoolObject> rolledObjects = new ArrayList<>();

            // roll objects to be spawned
            if (!explicitlyChanced.isEmpty()) {
                var roll = (float) RandomUtil.randChance();

                for (var obj : explicitlyChanced) {
                    roll -= obj.chance;

                    // Triggering object is marked as spawned at this time and can be also rolled (respawn case)
                    // so this need explicit check for this case
                    if (roll < 0 && (obj.guid == triggerFrom || !spawns.<T>IsSpawnedObject(obj.guid))) {
                        rolledObjects.add(obj);

                        break;
                    }
                }
            }

            if (!equalChanced.isEmpty() && rolledObjects.isEmpty()) {
                rolledObjects.addAll(equalChanced.where(obj -> obj.guid == triggerFrom || !spawns.<T>IsSpawnedObject(obj.guid)));
                rolledObjects.RandomResize((int) count);
            }

            // try to spawn rolled objects
            for (var obj : rolledObjects) {
                if (obj.guid == triggerFrom) {
                    reSpawn1Object(spawns, obj);
                    triggerFrom = 0;
                } else {
                    spawns.<T>AddSpawn(obj.guid, poolId);
                    spawn1Object(spawns, obj);
                }
            }
        }

        // One spawn one despawn no count increase
        if (triggerFrom != 0) {
            despawnObject(spawns, triggerFrom);
        }
    }

    public final boolean isEmpty() {
        return explicitlyChanced.isEmpty() && equalChanced.isEmpty();
    }

    public final int getPoolId() {
        return poolId;
    }

    public final void setPoolId(int pool_id) {
        poolId = pool_id;
    }

    private void despawn1Object(SpawnedPoolData spawns, long guid, boolean alwaysDeleteRespawnTime) {
        despawn1Object(spawns, guid, alwaysDeleteRespawnTime, true);
    }

    private void despawn1Object(SpawnedPoolData spawns, long guid) {
        despawn1Object(spawns, guid, false, true);
    }

    private void despawn1Object(SpawnedPoolData spawns, long guid, boolean alwaysDeleteRespawnTime, boolean saveRespawnTime) {
        switch (T.class.name) {
            case "Creature": {
                var creatureBounds = spawns.getMap().getCreatureBySpawnIdStore().get(guid);

                for (var i = creatureBounds.size() - 1; i > 0; i--) // this gets modified.
                {
                    if (creatureBounds.size() > i) {
                        var creature = creatureBounds[i];

                        // For dynamic spawns, save respawn time here
                        if (saveRespawnTime && !creature.getRespawnCompatibilityMode()) {
                            creature.saveRespawnTime();
                        }

                        creature.addObjectToRemoveList();
                    }
                }

                if (alwaysDeleteRespawnTime) {
                    spawns.getMap().removeRespawnTime(SpawnObjectType.CREATURE, guid, null, true);
                }

                break;
            }
            case "GameObject": {
                var gameobjectBounds = spawns.getMap().getGameObjectBySpawnIdStore().get(guid);

                for (var go : gameobjectBounds) {
                    // For dynamic spawns, save respawn time here
                    if (saveRespawnTime && !go.getRespawnCompatibilityMode()) {
                        go.saveRespawnTime();
                    }

                    go.addObjectToRemoveList();
                }

                if (alwaysDeleteRespawnTime) {
                    spawns.getMap().removeRespawnTime(SpawnObjectType.gameObject, guid, null, true);
                }

                break;
            }
            case "Pool":
                global.getPoolMgr().despawnPool(spawns, (int) guid, alwaysDeleteRespawnTime);

                break;
        }
    }

    private void spawn1Object(SpawnedPoolData spawns, PoolObject obj) {
        switch (T.class.name) {
            case "Creature": {
                var data = global.getObjectMgr().getCreatureData(obj.guid);

                if (data != null) {
                    // Spawn if necessary (loaded grids only)
                    // We use spawn coords to spawn
                    if (spawns.getMap().isGridLoaded(data.spawnPoint)) {
                        CREATURE.createCreatureFromDB(obj.guid, spawns.getMap());
                    }
                }
            }

            break;
            case "GameObject": {
                var data = global.getObjectMgr().getGameObjectData(obj.guid);

                if (data != null) {
                    // Spawn if necessary (loaded grids only)
                    // We use current coords to unspawn, not spawn coords since creature can have changed grid
                    if (spawns.getMap().isGridLoaded(data.spawnPoint)) {
                        var go = gameObject.createGameObjectFromDb(obj.guid, spawns.getMap(), false);

                        if (go && go.isSpawnedByDefault()) {
                            if (!spawns.getMap().addToMap(go)) {
                                go.close();
                            }
                        }
                    }
                }
            }

            break;
            case "Pool":
                global.getPoolMgr().spawnPool(spawns, (int) obj.guid);

                break;
        }
    }

    private void reSpawn1Object(SpawnedPoolData spawns, PoolObject obj) {
        switch (T.class.name) {
            case "Creature":
            case "GameObject":
                despawn1Object(spawns, obj.guid, false, false);
                spawn1Object(spawns, obj);

                break;
        }
    }


    private void removeRespawnTimeFromDB(SpawnedPoolData spawns, long guid) {
        switch (T.class.name) {
            case "Creature":
                spawns.getMap().removeRespawnTime(SpawnObjectType.CREATURE, guid, null, true);

                break;
            case "GameObject":
                spawns.getMap().removeRespawnTime(SpawnObjectType.gameObject, guid, null, true);

                break;
        }
    }
}
