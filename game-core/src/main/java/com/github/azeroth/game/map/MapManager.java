package com.github.azeroth.game.map;


// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.


import com.github.azeroth.common.Logs;
import com.github.azeroth.common.Pair;
import com.github.azeroth.dbc.defines.Difficulty;
import com.github.azeroth.dbc.domain.MapDifficulty;
import com.github.azeroth.game.domain.map.MapDb2Entries;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.object.WorldLocation;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.group.PlayerGroup;
import com.github.azeroth.game.world.WorldContext;

import java.util.BitSet;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class MapManager  {

    private final HashMap<Pair<Integer, Integer>, Map> maps = new HashMap<>();

    private final ReentrantLock mapsLock = new ReentrantLock();
    private final BitSet freeInstanceIds = new BitSet(1);

    private int gridCleanUpDelay;

    private int nextInstanceId;
    private int scheduledScripts;

    private WorldContext world;

    private MapManager(WorldContext world) {
        this.world = world;
        gridCleanUpDelay = world.getWorldSettings().intWorldConfig.getUIntValue(WorldCfg.IntervalGridclean);
        timer.setInterval(WorldConfig.getIntValue(WorldCfg.IntervalMapupdate));
    }



    public final void initializeVisibilityDistanceInfo() {
        for (var pair : maps.getValues()) {
            for (var map : pair.Values) {
                map.initVisibilityDistance();
            }
        }
    }


    public final Map createMap(int mapId, Player player) {
        if (player == null) {
            return null;
        }

        var entry = world.getDbcObjectManager().map(mapId);

        if (entry == null) {
            return null;
        }

        mapsLock.lock();
        try {
            Map map;
            int newInstanceId = 0;

            if (entry.isBattlegroundOrArena()) {
                // instantiate or find existing bg map for player
                // the instance id is set in battlegroundid
                newInstanceId = player.getBattlegroundId();

                if (newInstanceId == 0) {
                    return null;
                }

                map = findMapI(mapId, newInstanceId);

                if (map == null) {
                    var bg = player.getBattleground();

                    if (bg != null) {
                        map = createBattleground(mapId, newInstanceId, bg);
                    } else {
                        player.teleportToBGEntryPoint();

                        return null;
                    }
                }
            } else if (entry.isDungeon()) {
                var group = player.getGroup();
                var difficulty = group != null ? group.getDifficultyID(entry) : player.getDifficultyId(entry);

                MapDb2Entries entries = new MapDb2Entries(entry, world.getDbcObjectManager().getDownscaledMapDifficultyData(mapId, difficulty));
                difficulty = entries.mapDifficulty.getDifficulty();
                var instanceOwnerGuid = group != null ? group.getRecentInstanceOwner(mapId) : player.getGUID();
                var instanceLock = world.getInstanceLockManager().findActiveInstanceLock(instanceOwnerGuid, entries);

                if (instanceLock != null) {
                    newInstanceId = instanceLock.getInstanceId();

                    // Reset difficulty to the one used in instance lock
                    if (!entries.map.isFlexLocking()) {
                        difficulty = instanceLock.getDifficultyId();
                    }
                } else {
                    // Try finding instance id for normal dungeon
                    if (!entries.mapDifficulty.hasResetSchedule()) {
                        newInstanceId = group == null ? group.getRecentInstanceId(mapId) : player.getRecentInstanceId(mapId);
                    }

                    // If not found or instance is not a normal dungeon, generate new one
                    if (newInstanceId == 0) {
                        newInstanceId = generateInstanceId();
                    }

                    instanceLock = world.getInstanceLockManager().createInstanceLockForNewInstance(instanceOwnerGuid, entries, newInstanceId);
                }

                // it is possible that the save exists but the map doesn't
                map = findMapI(mapId, newInstanceId);

                // is is also possible that instance id is already in use by another group for boss-based locks
                if (!entries.isInstanceIdBound() && instanceLock != null && map != null && map.toInstanceMap().getInstanceLock() != instanceLock) {
                    newInstanceId = generateInstanceId();
                    instanceLock.setInstanceId(newInstanceId);
                    map = null;
                }

                if (map == null) {
                    map = createInstance(mapId, newInstanceId, instanceLock, difficulty, player.getTeamId(), group);

                    if (group) {
                        group.setRecentInstance(mapId, instanceOwnerGuid.clone(), newInstanceId);
                    } else {
                        player.setRecentInstance(mapId, newInstanceId);
                    }
                }
            } else if (entry.isGarrison()) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: newInstanceId = (uint)player.GUID.Counter;
                newInstanceId = (int) player.getGUID().counter();
                map = findMapI(mapId, newInstanceId);

                if (map == null) {
                    map = createGarrison(mapId, newInstanceId, player);
                }
            } else {
                newInstanceId = 0;

                if (entry.isSplitByFaction()) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: newInstanceId = (uint)player.TeamId;
                    newInstanceId = (int) player.getTeamId();
                }

                map = findMapI(mapId, newInstanceId);

                if (map == null) {
                    map = createWorldMap(mapId, newInstanceId);
                }
            }

            if (map != null) {
                maps.add(map.getId(), map.getInstanceId(), map);
            }

            return map;
        }finally {
            mapsLock.unlock();
        }
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public Map FindMap(uint mapId, uint instanceId)
    public final Map findMap(int mapId, int instanceId) {
        synchronized (mapsLock) {
            return findMapI(mapId, instanceId);
        }
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint FindInstanceIdForPlayer(uint mapId, Player player)
    public final int findInstanceIdForPlayer(int mapId, Player player) {
        var entry = world.getDbcObjectManager().map(mapId);

        if (entry == null) {
            return 0;
        }

        if (entry.isBattlegroundOrArena()) {
            return player.getBattlegroundId();
        } else if (entry.isDungeon()) {
            var group = player.getGroup();
            var difficulty = group != null ? group.getDifficultyID(entry) : player.getDifficultyId(entry);
            MapDb2Entries entries = new MapDb2Entries(entry, world.getDbcObjectManager().getDownscaledMapDifficultyData(mapId, difficulty));


            var instanceOwnerGuid = group != null ? group.getRecentInstanceOwner(mapId) : player.getGUID();
            var instanceLock = world.getInstanceLockManager().findActiveInstanceLock(instanceOwnerGuid, entries);

            int newInstanceId = 0;

            if (instanceLock != null) {
                newInstanceId = instanceLock.getInstanceId();
            } else if (!entries.mapDifficulty.hasResetSchedule()) { // Try finding instance id for normal dungeon
                newInstanceId = group != null ? group.getRecentInstanceId(mapId) : player.getRecentInstanceId(mapId);
            }

            if (newInstanceId == 0) {
                return 0;
            }

            var map = findMap(mapId, newInstanceId);

            // is is possible that instance id is already in use by another group for boss-based locks
            if (!entries.isInstanceIdBound() && instanceLock != null && map != null && map.toInstanceMap().getInstanceLock() != instanceLock) {
                return 0;
            }

            return newInstanceId;
        } else if (entry.isGarrison()) {

            return (int) player.getGUID().counter();
        } else {
            if (entry.isSplitByFaction()) {
                return player.getTeamId();
            }

            return 0;
        }
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public void Update(uint diff)
    public final void update(int diff) {
        timer.Update(diff);

        if (!timer.getPassed()) {
            return;
        }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var time = (uint)_timer.Current;
        var time = (int) timer.getCurrent();

        for (var mapkvp : maps) {
            for (var instanceKvp : mapkvp.Value) {
                if (instanceKvp.Value.CanUnload(diff)) {
                    updater.Schedule(() -> {
                        if (destroyMap(instanceKvp.Value)) {
                            maps.QueueRemove(mapkvp.Key, instanceKvp.Key);
                        }
                    });

                    continue;
                }

                updater.Schedule(() -> instanceKvp.Value.Update(time));
            }
        }

        updater.Wait();
        maps.ExecuteRemove();

        for (var kvp : maps.getValues()) {
            for (var map : kvp.Values) {
                updater.Stage(() -> map.DelayedUpdate(time));
            }
        }

        updater.Wait();
        timer.setCurrent(0);
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public bool IsValidMAP(uint mapId)
    public final boolean isValidMap(int mapId) {
        return CliDB.mapStorage.containsKey(mapId);
    }


    public final boolean isValidMapCoordinate(int mapId, float x, float y) {
        return isValidMap(mapId) && MapDefine.isValidMapCoordinate(x, y);
    }

    public final boolean isValidMapCoordinate(int mapId, float x, float y, float z) {
        return isValidMap(mapId) && MapDefine.isValidMapCoordinate(x, y, z);
    }

    public final boolean isValidMapCoordinate(int mapId, float x, float y, float z, float o) {
        return isValidMap(mapId) && MapDefine.isValidMapCoordinate(x, y, z, o);
    }

    public final boolean isValidMapCoordinate(int mapId, Position pos) {
        return isValidMapCoordinate(mapId, pos.getX(), pos.getY(), pos.getZ(), pos.getO());
    }

    public final boolean isValidMapCoordinate(WorldLocation loc) {
        return isValidMapCoordinate(loc.getMapId(), loc);
    }

    public final void unloadAll() {
        // first unload maps
        for (var pair : maps.getValues()) {
            for (var map : pair.Values) {
                map.UnloadAll();
            }
        }

        for (var pair : maps.getValues()) {
            for (var map : pair.Values) {
                map.Dispose();
            }
        }

        maps.Clear();

        if (updater != null) {
            updater.Deactivate();
        }
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint GetNumInstances()
    public final int getNumInstances() {
        synchronized (mapsLock) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: return (uint)_maps.Sum(pair => pair.Value.Count(kvp => kvp.Value.IsDungeon));
            return (int) maps.Sum(pair -> pair.Value.Count(kvp -> kvp.Value.IsDungeon));
        }
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint GetNumPlayersInInstances()
    public final int getNumPlayersInInstances() {
        synchronized (mapsLock) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: return (uint)_maps.Sum(pair => pair.Value.Sum(kvp => kvp.Value.IsDungeon ? kvp.Value.Players.Count : 0));
            return (int) maps.Sum(pair -> pair.Value.Sum(kvp -> kvp.Value.IsDungeon ? kvp.Value.Players.Count : 0));
        }
    }

    public final void initInstanceIds() {
        nextInstanceId = 1;

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: ulong maxExistingInstanceId = 0;
        long maxExistingInstanceId = 0;
        var result = DB.Characters.Query("SELECT IFNULL(MAX(instanceId), 0) FROM instance");

        if (!result.IsEmpty()) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: maxExistingInstanceId = Math.Max(maxExistingInstanceId, result.Read<ulong>(0));
            maxExistingInstanceId = Math.max(maxExistingInstanceId, result.<Long>Read(0));
        }

        result = DB.Characters.Query("SELECT IFNULL(MAX(instanceId), 0) FROM character_instance_lock");

        if (!result.IsEmpty()) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: maxExistingInstanceId = Math.Max(maxExistingInstanceId, result.Read<ulong>(0));
            maxExistingInstanceId = Math.max(maxExistingInstanceId, result.<Long>Read(0));
        }

        freeInstanceIds.setLength((int) (maxExistingInstanceId + 2)); // make space for one extra to be able to access [_nextInstanceId] index in case all slots are taken

        // never allow 0 id
        freeInstanceIds.set(0, false);
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public void RegisterInstanceId(uint instanceId)
    public final void registerInstanceId(int instanceId) {
        freeInstanceIds.set((int) instanceId, false);

        // Instances are pulled in ascending order from db and nextInstanceId is initialized with 1,
        // so if the instance id is used, increment until we find the first unused one for a potential new instance
        if (nextInstanceId == instanceId) {
            ++nextInstanceId;
        }
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint GenerateInstanceId()
    public final int generateInstanceId() {
        if (nextInstanceId == 0xFFFFFFFF) {
            Logs.MAPS.error("Instance ID overflow!! Can't continue, shutting down server. ");
            Global.getWorldMgr().stopNow();

            return nextInstanceId;
        }

        var newInstanceId = nextInstanceId;
        freeInstanceIds.set((int) newInstanceId, false);

        // Find the lowest available id starting from the current NextInstanceId (which should be the lowest according to the logic in FreeInstanceId()
        var nextFreeId = -1;

        for (var i = (int) nextInstanceId++; i < freeInstanceIds.getLength(); i++) {
            if (freeInstanceIds.get(i)) {
                nextFreeId = i;

                break;
            }
        }

        if (nextFreeId == -1) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: _nextInstanceId = (uint)_freeInstanceIds.Length;
            nextInstanceId = (int) freeInstanceIds.getLength();
            freeInstanceIds.setLength(freeInstanceIds.Length + 1);
            freeInstanceIds.set((int) nextInstanceId, true);
        } else {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: _nextInstanceId = (uint)nextFreeId;
            nextInstanceId = (int) nextFreeId;
        }

        return newInstanceId;
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public void FreeInstanceId(uint instanceId)
    public final void freeInstanceId(int instanceId) {
        // If freed instance id is lower than the next id available for new instances, use the freed one instead
        nextInstanceId = Math.min(instanceId, nextInstanceId);
        freeInstanceIds.set((int) instanceId, true);
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public void SetGridCleanUpDelay(uint t)
    public final void setGridCleanUpDelay(int t) {
        if (t < MapConst.MinGridDelay) {
            gridCleanUpDelay = MapConst.MinGridDelay;
        } else {
            gridCleanUpDelay = t;
        }
    }

    public final void setMapUpdateInterval(int t) {
        if (t < MapConst.MinMapUpdateDelay) {
            t = MapConst.MinMapUpdateDelay;
        }

        timer.setInterval(t);
        timer.Reset();
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint GetNextInstanceId()
    public final int getNextInstanceId() {
        return nextInstanceId;
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public void SetNextInstanceId(uint nextInstanceId)
    public final void setNextInstanceId(int nextInstanceId) {
        this.nextInstanceId = nextInstanceId;
    }

    public final void doForAllMaps(tangible.Action1Param<Map> worker) {
        synchronized (mapsLock) {
            for (var kvp : maps.getValues()) {
                for (var map : kvp.Values) {
                    worker.invoke(map);
                }
            }
        }
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public void DoForAllMapsWithMapId(uint mapId, Action<Map> worker)
    public final void doForAllMapsWithMapId(int mapId, tangible.Action1Param<Map> worker) {
        synchronized (mapsLock) {
            TValue instanceDict;
            tangible.OutObject<TValue> tempOutInstanceDict = new tangible.OutObject<TValue>();
            if (maps.TryGetValue(mapId, tempOut_instanceDict)) {
                instanceDict = tempOutInstanceDict.outArgValue;
                for (var kvp : instanceDict) {
                    if (kvp.Key >= 0) {
                        worker.invoke(kvp.Value);
                    }
                }
            } else {
                instanceDict = tempOutInstanceDict.outArgValue;
            }
        }
    }

    public final void addSCBuiltInScripts() {
        for (var(_, mapEntry) : CliDB.mapStorage) {
            if (mapEntry.IsWorldMap() && mapEntry.IsSplitByFaction()) {
                new SplitByFactionMapScript(String.format("world_map_set_faction_worldstates_%1$s", mapEntry.Id), mapEntry.Id);
            }
        }
    }

    public final void increaseScheduledScriptsCount() {
        ++scheduledScripts;
    }

    public final void decreaseScheduledScriptCount() {
        --scheduledScripts;
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public void DecreaseScheduledScriptCount(uint count)
    public final void decreaseScheduledScriptCount(int count) {
        scheduledScripts -= count;
    }

    public final boolean isScriptScheduled() {
        return scheduledScripts > 0;
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: Map FindMap_i(uint mapId, uint instanceId)
    private Map findMapI(int mapId, int instanceId) {
        var map;
//C# TO JAVA CONVERTER TODO TASK: The following method call contained an unresolved 'out' keyword - these cannot be converted using the 'OutObject' helper class unless the method is within the code being modified:
        return maps.TryGetValue(mapId, instanceId, out map) ? map : null;
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: Map CreateWorldMap(uint mapId, uint instanceId)
    private Map createWorldMap(int mapId, int instanceId) {
        var map = new Map(mapId, gridCleanUpDelay, instanceId, Difficulty.None);
        map.loadRespawnTimes();
        map.loadCorpseData();

        if (WorldConfig.getBoolValue(WorldCfg.BasemapLoadGrids)) {
            map.loadAllCells();
        }

        return map;
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: InstanceMap CreateInstance(uint mapId, uint instanceId, InstanceLock instanceLock, Difficulty difficulty, int team, PlayerGroup group)
    private InstanceMap createInstance(int mapId, int instanceId, InstanceLock instanceLock, Difficulty difficulty, int team, PlayerGroup group) {
        // make sure we have a valid map id
        var entry = world.getDbcObjectManager().map(mapId);

        if (entry == null) {
            Logs.MAPS.error("CreateInstance: no entry for map {}", mapId);

            //ABORT();
            return null;
        }

        // some instances only have one difficulty
        MapDifficulty mapDifficulty = world.getDbcObjectManager().getDownscaledMapDifficultyData(mapId, difficulty);
        difficulty = mapDifficulty.getDifficulty();

        Logs.MAPS.debug(String.format("MapInstanced::CreateInstance: %1$smap instance %2$s for %3$s created with difficulty %4$s", (instanceLock == null ? null : instanceLock.getInstanceId() != 0 ? "" : "new "), instanceId, mapId, difficulty));

        
        var map = new InstanceMap(mapId, gridCleanUpDelay, instanceId, difficulty, team, instanceLock);

        map.loadRespawnTimes();
        map.loadCorpseData();

        if (group != null) {
            map.trySetOwningGroup(group);
        }

        map.createInstanceData();
        map.setInstanceScenario(Global.getScenarioMgr().createInstanceScenario(map, team));

        if (WorldConfig.getBoolValue(WorldCfg.InstancemapLoadGrids)) {
            map.loadAllCells();
        }

        return map;
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: BattlegroundMap CreateBattleground(uint mapId, uint instanceId, Battleground bg)
    private BattlegroundMap createBattleground(int mapId, int instanceId, Battleground bg) {
        Logs.MAPS.debug(String.format("MapInstanced::CreateBattleground: map bg {} for {} created.", instanceId, mapId));

        var map = new BattlegroundMap(mapId, gridCleanUpDelay, instanceId, Difficulty.None);
        map.setBG(bg);
        bg.setBgMap(map);

        return map;
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: GarrisonMap CreateGarrison(uint mapId, uint instanceId, Player owner)
    private GarrisonMap createGarrison(int mapId, int instanceId, Player owner) {
        var map = new GarrisonMap(mapId, gridCleanUpDelay, instanceId, owner.getGUID().clone());

        return map;
    }

    private boolean destroyMap(Map map) {
        map.removeAllPlayers();

        if (map.havePlayers()) {
            return false;
        }

        map.unloadAll();

        // Free up the instance id and allow it to be reused for normal dungeons, bgs and arenas
        if (map.isBattlegroundOrArena() || (map.isDungeon() && !map.getMapDifficulty().hasResetSchedule())) {
            freeInstanceId(map.getInstanceId());
        }

        // erase map
        map.destroy();

        return true;
    }
}

// hack to allow conditions to access what faction owns the map (these worldstates should not be set on these maps)