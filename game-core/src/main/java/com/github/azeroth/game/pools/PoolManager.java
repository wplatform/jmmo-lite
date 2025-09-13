package com.github.azeroth.game.pools;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.map.Map;

import java.util.HashMap;


public class PoolManager {
    private final HashMap<Integer, PoolTemplateData> poolTemplate = new HashMap<Integer, PoolTemplateData>();
    private final HashMap<Integer, PoolGroup<Creature>> poolCreatureGroups = new HashMap<Integer, PoolGroup<Creature>>();
    private final HashMap<Integer, PoolGroup<GameObject>> poolGameobjectGroups = new HashMap<Integer, PoolGroup<GameObject>>();
    private final HashMap<Integer, PoolGroup<Pool>> poolPoolGroups = new HashMap<Integer, PoolGroup<Pool>>();
    private final HashMap<Long, Integer> creatureSearchMap = new HashMap<Long, Integer>();
    private final HashMap<Long, Integer> gameobjectSearchMap = new HashMap<Long, Integer>();
    private final HashMap<Long, Integer> poolSearchMap = new HashMap<Long, Integer>();
    private final MultiMap<Integer, Integer> autoSpawnPoolsPerMap = new MultiMap<Integer, Integer>();
    public MultiMap<Integer, Integer> questCreatureRelation = new MultiMap<Integer, Integer>();
    public MultiMap<Integer, Integer> questGORelation = new MultiMap<Integer, Integer>();
    private PoolManager() {
    }

    public final void initialize() {
        gameobjectSearchMap.clear();
        creatureSearchMap.clear();
    }

    public final void loadFromDB() {
        {
            // Pool templates
            var oldMSTime = System.currentTimeMillis();

            var result = DB.World.query("SELECT entry, max_limit FROM pool_template");

            if (result.isEmpty()) {
                poolTemplate.clear();
                Log.outInfo(LogFilter.ServerLoading, "Loaded 0 object pools. DB table `pool_template` is empty.");

                return;
            }

            int count = 0;

            do {
                var pool_id = result.<Integer>Read(0);

                PoolTemplateData pPoolTemplate = new PoolTemplateData();
                pPoolTemplate.maxLimit = result.<Integer>Read(1);
                pPoolTemplate.mapId = -1;
                poolTemplate.put(pool_id, pPoolTemplate);
                ++count;
            } while (result.NextRow());

            Log.outInfo(LogFilter.ServerLoading, "Loaded {0} objects pools in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
        }

        // Creatures

        Log.outInfo(LogFilter.ServerLoading, "Loading Creatures Pooling data...");

        {
            var oldMSTime = System.currentTimeMillis();

            //                                         1        2            3
            var result = DB.World.query("SELECT spawnId, poolSpawnId, chance FROM pool_members WHERE type = 0");

            if (result.isEmpty()) {
                Log.outInfo(LogFilter.ServerLoading, "Loaded 0 creatures in  pools. DB table `pool_creature` is empty.");
            } else {
                int count = 0;

                do {
                    var guid = result.<Long>Read(0);
                    var pool_id = result.<Integer>Read(1);
                    var chance = result.<Float>Read(2);

                    var data = global.getObjectMgr().getCreatureData(guid);

                    if (data == null) {
                        Logs.SQL.error("`pool_creature` has a non existing creature spawn (GUID: {0}) defined for pool id ({1}), skipped.", guid, pool_id);

                        continue;
                    }

                    if (!poolTemplate.containsKey(pool_id)) {
                        Logs.SQL.error("`pool_creature` pool id ({0}) is not in `pool_template`, skipped.", pool_id);

                        continue;
                    }

                    if (chance < 0 || chance > 100) {
                        Logs.SQL.error("`pool_creature` has an invalid chance ({0}) for creature guid ({1}) in pool id ({2}), skipped.", chance, guid, pool_id);

                        continue;
                    }

                    var pPoolTemplate = poolTemplate.get(pool_id);

                    if (pPoolTemplate.mapId == -1) {
                        pPoolTemplate.mapId = (int) data.getMapId();
                    }

                    if (pPoolTemplate.mapId != data.getMapId()) {
                        Logs.SQL.error(String.format("`pool_creature` has creature spawns on multiple different maps for creature guid (%1$s) in pool id (%2$s), skipped.", guid, pool_id));

                        continue;
                    }

                    PoolObject plObject = new PoolObject(guid, chance);

                    if (!poolCreatureGroups.containsKey(pool_id)) {
                        poolCreatureGroups.put(pool_id, new PoolGroup<Creature>());
                    }

                    var cregroup = poolCreatureGroups.get(pool_id);
                    cregroup.setPoolId(pool_id);
                    cregroup.addEntry(plObject, pPoolTemplate.maxLimit);

                    creatureSearchMap.put(guid, pool_id);
                    ++count;
                } while (result.NextRow());

                Log.outInfo(LogFilter.ServerLoading, "Loaded {0} creatures in pools in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
            }
        }

        // Gameobjects

        Log.outInfo(LogFilter.ServerLoading, "Loading Gameobject Pooling data...");

        {
            var oldMSTime = System.currentTimeMillis();

            //                                         1        2            3
            var result = DB.World.query("SELECT spawnId, poolSpawnId, chance FROM pool_members WHERE type = 1");

            if (result.isEmpty()) {
                Log.outInfo(LogFilter.ServerLoading, "Loaded 0 gameobjects in  pools. DB table `pool_gameobject` is empty.");
            } else {
                int count = 0;

                do {
                    var guid = result.<Long>Read(0);
                    var pool_id = result.<Integer>Read(1);
                    var chance = result.<Float>Read(2);

                    var data = global.getObjectMgr().getGameObjectData(guid);

                    if (data == null) {
                        Logs.SQL.error("`pool_gameobject` has a non existing gameobject spawn (GUID: {0}) defined for pool id ({1}), skipped.", guid, pool_id);

                        continue;
                    }

                    if (!poolTemplate.containsKey(pool_id)) {
                        Logs.SQL.error("`pool_gameobject` pool id ({0}) is not in `pool_template`, skipped.", pool_id);

                        continue;
                    }

                    if (chance < 0 || chance > 100) {
                        Logs.SQL.error("`pool_gameobject` has an invalid chance ({0}) for gameobject guid ({1}) in pool id ({2}), skipped.", chance, guid, pool_id);

                        continue;
                    }

                    var pPoolTemplate = poolTemplate.get(pool_id);

                    if (pPoolTemplate.mapId == -1) {
                        pPoolTemplate.mapId = (int) data.getMapId();
                    }

                    if (pPoolTemplate.mapId != data.getMapId()) {
                        Logs.SQL.error(String.format("`pool_gameobject` has gameobject spawns on multiple different maps for gameobject guid (%1$s) in pool id (%2$s), skipped.", guid, pool_id));

                        continue;
                    }

                    PoolObject plObject = new PoolObject(guid, chance);

                    if (!poolGameobjectGroups.containsKey(pool_id)) {
                        poolGameobjectGroups.put(pool_id, new PoolGroup<GameObject>());
                    }

                    var gogroup = poolGameobjectGroups.get(pool_id);
                    gogroup.setPoolId(pool_id);
                    gogroup.addEntry(plObject, pPoolTemplate.maxLimit);

                    gameobjectSearchMap.put(guid, pool_id);
                    ++count;
                } while (result.NextRow());

                Log.outInfo(LogFilter.ServerLoading, "Loaded {0} gameobject in pools in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
            }
        }

        // Pool of pools

        Log.outInfo(LogFilter.ServerLoading, "Loading Mother Pooling data...");

        {
            var oldMSTime = System.currentTimeMillis();

            //                                         1        2            3
            var result = DB.World.query("SELECT spawnId, poolSpawnId, chance FROM pool_members WHERE type = 2");

            if (result.isEmpty()) {
                Log.outInfo(LogFilter.ServerLoading, "Loaded 0 pools in pools");
            } else {
                int count = 0;

                do {
                    var child_pool_id = result.<Integer>Read(0);
                    var mother_pool_id = result.<Integer>Read(1);
                    var chance = result.<Float>Read(2);

                    if (!poolTemplate.containsKey(mother_pool_id)) {
                        Logs.SQL.error("`pool_pool` mother_pool id ({0}) is not in `pool_template`, skipped.", mother_pool_id);

                        continue;
                    }

                    if (!poolTemplate.containsKey(child_pool_id)) {
                        Logs.SQL.error("`pool_pool` included pool_id ({0}) is not in `pool_template`, skipped.", child_pool_id);

                        continue;
                    }

                    if (mother_pool_id == child_pool_id) {
                        Logs.SQL.error("`pool_pool` pool_id ({0}) includes itself, dead-lock detected, skipped.", child_pool_id);

                        continue;
                    }

                    if (chance < 0 || chance > 100) {
                        Logs.SQL.error("`pool_pool` has an invalid chance ({0}) for pool id ({1}) in mother pool id ({2}), skipped.", chance, child_pool_id, mother_pool_id);

                        continue;
                    }

                    var pPoolTemplateMother = poolTemplate.get(mother_pool_id);
                    PoolObject plObject = new PoolObject(child_pool_id, chance);

                    if (!poolPoolGroups.containsKey(mother_pool_id)) {
                        poolPoolGroups.put(mother_pool_id, new PoolGroup<Pool>());
                    }

                    var plgroup = poolPoolGroups.get(mother_pool_id);
                    plgroup.setPoolId(mother_pool_id);
                    plgroup.addEntry(plObject, pPoolTemplateMother.maxLimit);

                    poolSearchMap.put(child_pool_id, mother_pool_id);
                    ++count;
                } while (result.NextRow());

                // Now check for circular reference
                // All pool_ids are in pool_template

                for (var(id, poolData) : poolTemplate) {
                    ArrayList<Integer> checkedPools = new ArrayList<>();
                    var poolItr = poolSearchMap.get(id);

                    while (poolItr != 0) {
                        if (poolData.mapId != -1) {
                            if (poolTemplate.get(poolItr).mapId == -1) {
                                poolTemplate.get(poolItr).mapId = poolData.mapId;
                            }

                            if (poolTemplate.get(poolItr).mapId != poolData.mapId) {
                                Logs.SQL.error(String.format("`pool_pool` has child pools on multiple maps in pool id (%1$s), skipped.", poolItr));
                                poolPoolGroups.get(poolItr).removeOneRelation(id);
                                poolSearchMap.remove(poolItr);
                                --count;

                                break;
                            }
                        }

                        checkedPools.add(id);

                        if (checkedPools.contains(poolItr)) {
                            var ss = "The pool(s) ";

                            for (var itr : checkedPools) {
                                ss += String.format("%1$s ", itr);
                            }

                            ss += String.format("create(s) a circular reference, which can cause the server to freeze.\nRemoving the last link between mother pool %1$s and child pool %2$s", id, poolItr);
                            Logs.SQL.error(ss);
                            poolPoolGroups.get(poolItr).removeOneRelation(id);
                            poolSearchMap.remove(poolItr);
                            --count;

                            break;
                        }

                        poolItr = poolSearchMap.get(poolItr);
                    }
                }

                Log.outInfo(LogFilter.ServerLoading, "Loaded {0} pools in mother pools in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
            }
        }


        for (var(poolId, templateData) : poolTemplate) {
            if (isEmpty(poolId)) {
                Logs.SQL.error(String.format("Pool Id %1$s is empty (has no creatures and no gameobects and either no child pools or child pools are all empty. The pool will not be spawned", poolId));

                continue;
            }
        }

        // The initialize method will spawn all pools not in an event and not in another pool, this is why there is 2 left joins with 2 null checks
        Log.outInfo(LogFilter.ServerLoading, "Starting objects pooling system...");

        {
            var oldMSTime = System.currentTimeMillis();

            var result = DB.World.query("SELECT DISTINCT pool_template.entry, pool_members.spawnId, pool_members.poolSpawnId FROM pool_template" + " LEFT JOIN game_event_pool ON pool_template.entry=game_event_pool.pool_entry" + " LEFT JOIN pool_members ON pool_members.type = 2 AND pool_template.entry = pool_members.spawnId WHERE game_event_pool.pool_entry IS NULL");

            if (result.isEmpty()) {
                Log.outInfo(LogFilter.ServerLoading, "Pool handling system initialized, 0 pools spawned.");
            } else {
                int count = 0;

                do {
                    var pool_entry = result.<Integer>Read(0);
                    var pool_pool_id = result.<Integer>Read(1);

                    if (isEmpty(pool_entry)) {
                        continue;
                    }

                    if (!checkPool(pool_entry)) {
                        if (pool_pool_id != 0) {
                            // The pool is a child pool in pool_pool table. Ideally we should remove it from the pool handler to ensure it never gets spawned,
                            // however that could recursively invalidate entire chain of mother pools. It can be done in the future but for now we'll do nothing.
                            Logs.SQL.error("Pool Id {0} has no equal chance pooled entites defined and explicit chance sum is not 100. This broken pool is a child pool of Id {1} and cannot be safely removed.", pool_entry, result.<Integer>Read(2));
                        } else {
                            Logs.SQL.error("Pool Id {0} has no equal chance pooled entites defined and explicit chance sum is not 100. The pool will not be spawned.", pool_entry);
                        }

                        continue;
                    }

                    // Don't spawn child pools, they are spawned recursively by their parent pools
                    if (pool_pool_id == 0) {
                        autoSpawnPoolsPerMap.add((int) poolTemplate.get(pool_entry).mapId, pool_entry);
                        count++;
                    }
                } while (result.NextRow());

                Log.outInfo(LogFilter.ServerLoading, "Pool handling system initialized, {0} pools will be spawned by default in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
            }
        }
    }

    public final void spawnPool(SpawnedPoolData spawnedPoolData, int pool_id) {
        this.<Pool>SpawnPool(spawnedPoolData, pool_id, 0);
        this.<GameObject>SpawnPool(spawnedPoolData, pool_id, 0);
        this.<Creature>SpawnPool(spawnedPoolData, pool_id, 0);
    }

    public final void despawnPool(SpawnedPoolData spawnedPoolData, int pool_id) {
        despawnPool(spawnedPoolData, pool_id, false);
    }

    public final void despawnPool(SpawnedPoolData spawnedPoolData, int pool_id, boolean alwaysDeleteRespawnTime) {
        if (poolCreatureGroups.containsKey(pool_id) && !poolCreatureGroups.get(pool_id).isEmpty()) {
            poolCreatureGroups.get(pool_id).despawnObject(spawnedPoolData, 0, alwaysDeleteRespawnTime);
        }

        if (poolGameobjectGroups.containsKey(pool_id) && !poolGameobjectGroups.get(pool_id).isEmpty()) {
            poolGameobjectGroups.get(pool_id).despawnObject(spawnedPoolData, 0, alwaysDeleteRespawnTime);
        }

        if (poolPoolGroups.containsKey(pool_id) && !poolPoolGroups.get(pool_id).isEmpty()) {
            poolPoolGroups.get(pool_id).despawnObject(spawnedPoolData, 0, alwaysDeleteRespawnTime);
        }
    }

    public final boolean isEmpty(int pool_id) {
        TValue gameobjectPool;
        if ((poolGameobjectGroups.containsKey(pool_id) && (gameobjectPool = poolGameobjectGroups.get(pool_id)) == gameobjectPool) && !gameobjectPool.isEmptyDeepCheck()) {
            return false;
        }

        TValue creaturePool;
        if ((poolCreatureGroups.containsKey(pool_id) && (creaturePool = poolCreatureGroups.get(pool_id)) == creaturePool) && !creaturePool.isEmptyDeepCheck()) {
            return false;
        }

        TValue pool;
        if ((poolPoolGroups.containsKey(pool_id) && (pool = poolPoolGroups.get(pool_id)) == pool) && !pool.isEmptyDeepCheck()) {
            return false;
        }

        return true;
    }

    public final boolean checkPool(int pool_id) {
        if (poolGameobjectGroups.containsKey(pool_id) && !poolGameobjectGroups.get(pool_id).checkPool()) {
            return false;
        }

        if (poolCreatureGroups.containsKey(pool_id) && !poolCreatureGroups.get(pool_id).checkPool()) {
            return false;
        }

        if (poolPoolGroups.containsKey(pool_id) && !poolPoolGroups.get(pool_id).checkPool()) {
            return false;
        }

        return true;
    }

    public final <T> void updatePool(SpawnedPoolData spawnedPoolData, int pool_id, long db_guid_or_pool_id) {
        var motherpoolid = this.<Pool>IsPartOfAPool(pool_id);

        if (motherpoolid != 0) {
            this.<Pool>SpawnPool(spawnedPoolData, motherpoolid, pool_id);
        } else {
            this.<T>SpawnPool(spawnedPoolData, pool_id, db_guid_or_pool_id);
        }
    }

    public final void updatePool(SpawnedPoolData spawnedPoolData, int pool_id, SpawnObjectType type, long spawnId) {
        switch (type) {
            case Creature:
                this.<Creature>UpdatePool(spawnedPoolData, pool_id, spawnId);

                break;
            case GameObject:
                this.<GameObject>UpdatePool(spawnedPoolData, pool_id, spawnId);

                break;
        }
    }

    public final SpawnedPoolData initPoolsForMap(Map map) {
        SpawnedPoolData spawnedPoolData = new SpawnedPoolData(map);
        var poolIds = autoSpawnPoolsPerMap.get(spawnedPoolData.getMap().getId());

        if (poolIds != null) {
            for (var poolId : poolIds) {
                spawnPool(spawnedPoolData, poolId);
            }
        }

        return spawnedPoolData;
    }

    public final PoolTemplateData getPoolTemplate(int pool_id) {
        return poolTemplate.get(pool_id);
    }

    public final <T> int isPartOfAPool(long db_guid) {
        switch (T.class.name) {
            case "Creature":
                return creatureSearchMap.get(db_guid);
            case "GameObject":
                return gameobjectSearchMap.get(db_guid);
            case "Pool":
                return poolSearchMap.get(db_guid);
        }

        return 0;
    }

    // Selects proper template overload to call based on passed type
    public final int isPartOfAPool(SpawnObjectType type, long spawnId) {
        switch (type) {
            case Creature:
                return this.<Creature>IsPartOfAPool(spawnId);
            case GameObject:
                return this.<GameObject>IsPartOfAPool(spawnId);
            case AreaTrigger:
                return 0;
            default:
                return 0;
        }
    }

    public final <T> boolean isSpawnedObject(long db_guid_or_pool_id) {
        switch (T.class.name) {
            case "Creature":
                return creatureSearchMap.containsKey(db_guid_or_pool_id);
            case "GameObject":
                return gameobjectSearchMap.containsKey(db_guid_or_pool_id);
            case "Pool":
                return poolSearchMap.containsKey(db_guid_or_pool_id);
        }

        return false;
    }

    private <T> void spawnPool(SpawnedPoolData spawnedPoolData, int pool_id, long db_guid) {
        switch (T.class.name) {
            case "Creature":
                if (poolCreatureGroups.containsKey(pool_id) && !poolCreatureGroups.get(pool_id).isEmpty()) {
                    poolCreatureGroups.get(pool_id).spawnObject(spawnedPoolData, poolTemplate.get(pool_id).maxLimit, db_guid);
                }

                break;
            case "GameObject":
                if (poolGameobjectGroups.containsKey(pool_id) && !poolGameobjectGroups.get(pool_id).isEmpty()) {
                    poolGameobjectGroups.get(pool_id).spawnObject(spawnedPoolData, poolTemplate.get(pool_id).maxLimit, db_guid);
                }

                break;
            case "Pool":
                if (poolPoolGroups.containsKey(pool_id) && !poolPoolGroups.get(pool_id).isEmpty()) {
                    poolPoolGroups.get(pool_id).spawnObject(spawnedPoolData, poolTemplate.get(pool_id).maxLimit, db_guid);
                }

                break;
        }
    }

    public enum QuestTypes {
        NONE(0),
        Daily(1),
        Weekly(2);

        public static final int SIZE = Integer.SIZE;
        private static HashMap<Integer, QuestTypes> mappings;
        private int intValue;

        private QuestTypes(int value) {
            intValue = value;
            getMappings().put(value, this);
        }

        private static HashMap<Integer, QuestTypes> getMappings() {
            if (mappings == null) {
                synchronized (QuestTypes.class) {
                    if (mappings == null) {
                        mappings = new HashMap<Integer, QuestTypes>();
                    }
                }
            }
            return mappings;
        }

        public static QuestTypes forValue(int value) {
            return getMappings().get(value);
        }

        public int getValue() {
            return intValue;
        }
    }
}

// for Pool of Pool case

