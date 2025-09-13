package com.github.azeroth.game.map;


import com.github.azeroth.common.Pair;
import com.github.azeroth.game.domain.map.MapDb2Entries;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.map.enums.TransferAbortReason;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;



//using InstanceLockKey = Tuple<uint, uint>;

public class InstanceLockManager {
    private final object lockobject = new object();
    private final HashMap<ObjectGuid, HashMap<Pair<Integer, Integer>, InstanceLock>> temporaryInstanceLocksByPlayer = new HashMap<>(); // locks stored here before any boss gets killed
    private final HashMap<ObjectGuid, HashMap<Pair<Integer, Integer>, InstanceLock>> instanceLocksByPlayer = new HashMap<>();
    private final HashMap<Integer, SharedInstanceLockData> instanceLockDataById = new HashMap<Integer, SharedInstanceLockData>();
    private boolean unloading;

    private InstanceLockManager() {
    }

    public final void load() {
        HashMap<Integer, SharedInstanceLockData> instanceLockDataById = new HashMap<Integer, SharedInstanceLockData>();

        //                                              0           1     2
        var result = DB.characters.query("SELECT instanceId, data, completedEncountersMask FROM instance");

        if (!result.isEmpty()) {
            do {
                var instanceId = result.<Integer>Read(0);

                SharedInstanceLockData data = new SharedInstanceLockData();
                data.setData(result.<String>Read(1));
                data.setCompletedEncountersMask(result.<Integer>Read(2));
                data.instanceId = instanceId;

                instanceLockDataById.put(instanceId, data);
            } while (result.NextRow());
        }

        //                                                  0     1      2       3           4           5     6                        7           8
        var lockResult = DB.characters.query("SELECT guid, mapId, lockId, instanceId, difficulty, data, completedEncountersMask, expiryTime, extended FROM character_instance_lock");

        if (!result.isEmpty()) {
            do {
                var playerGuid = ObjectGuid.create(HighGuid.Player, lockResult.<Long>Read(0));
                var mapId = lockResult.<Integer>Read(1);
                var lockId = lockResult.<Integer>Read(2);
                var instanceId = lockResult.<Integer>Read(3);
                var difficulty = Difficulty.forValue(lockResult.<Byte>Read(4));
                var expiryTime = time.UnixTimeToDateTime(lockResult.<Long>Read(7));

                // Mark instance id as being used
                global.getMapMgr().RegisterInstanceId(instanceId);

                InstanceLock instanceLock;

                if ((new MapDb2Entries(mapId, difficulty)).IsInstanceIdBound()) {
                    var sharedData = instanceLockDataById.get(instanceId);

                    if (sharedData == null) {
                        Log.outError(LogFilter.instance, String.format("Missing instance data for instance id based lock (id %1$s)", instanceId));
                        DB.characters.execute(String.format("DELETE FROM character_instance_lock WHERE instanceId = %1$s", instanceId));

                        continue;
                    }

                    instanceLock = new SharedInstanceLock(mapId, difficulty, expiryTime, instanceId, sharedData);
                    instanceLockDataById.put(instanceId, sharedData);
                } else {
                    instanceLock = new InstanceLock(mapId, difficulty, expiryTime, instanceId);
                }

                instanceLock.getData().setData(lockResult.<String>Read(5));
                instanceLock.getData().setCompletedEncountersMask(lockResult.<Integer>Read(6));
                instanceLock.setExtended(lockResult.<Boolean>Read(8));

                instanceLocksByPlayer.get(playerGuid).put(Tuple.create(mapId, lockId), instanceLock);
            } while (result.NextRow());
        }
    }

    public final void unload() {
        unloading = true;
        instanceLocksByPlayer.clear();
        instanceLockDataById.clear();
    }

    public final TransferAbortReason canJoinInstanceLock(ObjectGuid playerGuid, MapDb2Entries entries, InstanceLock instanceLock) {
        if (!entries.mapDifficulty.hasResetSchedule()) {
            return TransferAbortReason.NONE;
        }

        var playerInstanceLock = findActiveInstanceLock(playerGuid, entries);

        if (playerInstanceLock == null) {
            return TransferAbortReason.NONE;
        }

        if (entries.map.isFlexLocking()) {
            // compare completed encounters - if instance has any encounters unkilled in players lock then cannot enter
            if ((playerInstanceLock.getData().getCompletedEncountersMask() & ~instanceLock.getData().getCompletedEncountersMask()) != 0) {
                return TransferAbortReason.ALREADY_COMPLETED_ENCOUNTER;
            }

            return TransferAbortReason.NONE;
        }

        if (!entries.mapDifficulty.isUsingEncounterLocks() && playerInstanceLock.getInstanceId() != 0 && playerInstanceLock.getInstanceId() != instanceLock.getInstanceId()) {
            return TransferAbortReason.LOCKED_TO_DIFFERENT_INSTANCE;
        }

        return TransferAbortReason.NONE;
    }

    public final InstanceLock findInstanceLock(HashMap<ObjectGuid, HashMap<Pair<Integer, Integer>, InstanceLock>> locks, ObjectGuid playerGuid, MapDb2Entries entries) {
        var playerLocks = locks.get(playerGuid);

        if (playerLocks == null) {
            return null;
        }

        return playerLocks.get(entries.GetKey());
    }

    public final InstanceLock findActiveInstanceLock(ObjectGuid playerGuid, MapDb2Entries entries) {
        synchronized (lockObject) {
            return findActiveInstanceLock(playerGuid, entries, false, true);
        }
    }

    public final InstanceLock findActiveInstanceLock(ObjectGuid playerGuid, MapDb2Entries entries, boolean ignoreTemporary, boolean ignoreExpired) {
        var instanceLock = findInstanceLock(instanceLocksByPlayer, playerGuid, entries);

        // Ignore expired and not extended locks
        if (instanceLock != null && (!instanceLock.isExpired() || instanceLock.isExtended() || !ignoreExpired)) {
            return instanceLock;
        }

        if (ignoreTemporary) {
            return null;
        }

        return findInstanceLock(temporaryInstanceLocksByPlayer, playerGuid, entries);
    }

    public final Collection<InstanceLock> getInstanceLocksForPlayer(ObjectGuid playerGuid) {
        TValue dictionary;
        if (instanceLocksByPlayer.containsKey(playerGuid) && (dictionary = instanceLocksByPlayer.get(playerGuid)) == dictionary) {
            return dictionary.VALUES;
        }

        return new ArrayList<>();
    }

    public final InstanceLock createInstanceLockForNewInstance(ObjectGuid playerGuid, MapDb2Entries entries, int instanceId) {
        if (!entries.MapDifficulty.HasResetSchedule()) {
            return null;
        }

        InstanceLock instanceLock;

        if (entries.IsInstanceIdBound()) {
            SharedInstanceLockData sharedData = new SharedInstanceLockData();
            instanceLockDataById.put(instanceId, sharedData);

            instanceLock = new SharedInstanceLock(entries.MapDifficulty.mapID, Difficulty.forValue((byte) entries.MapDifficulty.difficultyID), getNextResetTime(entries), 0, sharedData);
        } else {
            instanceLock = new InstanceLock(entries.MapDifficulty.mapID, Difficulty.forValue((byte) entries.MapDifficulty.difficultyID), getNextResetTime(entries), 0);
        }

        if (!temporaryInstanceLocksByPlayer.containsKey(playerGuid)) {
            temporaryInstanceLocksByPlayer.put(playerGuid, new HashMap<Tuple<Integer, Integer>, InstanceLock>());
        }

        temporaryInstanceLocksByPlayer.get(playerGuid).put(entries.GetKey(), instanceLock);

        Log.outDebug(LogFilter.instance, String.format("[%1$s-%2$s | ", entries.Map.id, entries.Map.MapName.get(global.getWorldMgr().getDefaultDbcLocale())) + String.format("%1$s-%2$s] Created new temporary instance lock for %3$s in instance %4$s", entries.MapDifficulty.difficultyID, CliDB.DifficultyStorage.get(entries.MapDifficulty.difficultyID).name, playerGuid, instanceId));

        return instanceLock;
    }

    public final InstanceLock updateInstanceLockForPlayer(SQLTransaction trans, ObjectGuid playerGuid, MapDb2Entries entries, InstanceLockUpdateEvent updateEvent) {
        var instanceLock = findActiveInstanceLock(playerGuid, entries, true, true);

        if (instanceLock == null) {
            synchronized (lockObject) {
                // Move lock from temporary storage if it exists there
                // This is to avoid destroying expired locks before any boss is killed in a fresh lock
                // player can still change his mind, exit instance and reactivate old lock
                var playerLocks = temporaryInstanceLocksByPlayer.get(playerGuid);

                if (playerLocks != null) {
                    var playerInstanceLock = playerLocks.get(entries.GetKey());

                    if (playerInstanceLock != null) {
                        instanceLock = playerInstanceLock;
                        instanceLocksByPlayer.get(playerGuid).put(entries.GetKey(), instanceLock);

                        playerLocks.remove(entries.GetKey());

                        if (playerLocks.isEmpty()) {
                            temporaryInstanceLocksByPlayer.remove(playerGuid);
                        }

                        Log.outDebug(LogFilter.instance, String.format("[%1$s-%2$s | ", entries.Map.id, entries.Map.MapName.get(global.getWorldMgr().getDefaultDbcLocale())) + String.format("%1$s-%2$s] Promoting temporary lock to permanent for %3$s in instance %4$s", entries.MapDifficulty.difficultyID, CliDB.DifficultyStorage.get(entries.MapDifficulty.difficultyID).name, playerGuid, updateEvent.instanceId));
                    }
                }
            }
        }

        if (instanceLock == null) {
            if (entries.IsInstanceIdBound()) {
                var sharedDataItr = instanceLockDataById.get(updateEvent.instanceId);

                instanceLock = new SharedInstanceLock(entries.MapDifficulty.mapID, Difficulty.forValue((byte) entries.MapDifficulty.difficultyID), getNextResetTime(entries), updateEvent.instanceId, sharedDataItr);
            } else {
                instanceLock = new InstanceLock(entries.MapDifficulty.mapID, Difficulty.forValue((byte) entries.MapDifficulty.difficultyID), getNextResetTime(entries), updateEvent.instanceId);
            }

            synchronized (lockObject) {
                instanceLocksByPlayer.get(playerGuid).put(entries.GetKey(), instanceLock);
            }

            Log.outDebug(LogFilter.instance, String.format("[%1$s-%2$s | ", entries.Map.id, entries.Map.MapName.get(global.getWorldMgr().getDefaultDbcLocale())) + String.format("%1$s-%2$s] Created new instance lock for %3$s in instance %4$s", entries.MapDifficulty.difficultyID, CliDB.DifficultyStorage.get(entries.MapDifficulty.difficultyID).name, playerGuid, updateEvent.instanceId));
        } else {
            instanceLock.setInstanceId(updateEvent.instanceId);
        }

        instanceLock.getData().setData(updateEvent.newData);

        if (updateEvent.completedEncounter != null) {
            instanceLock.getData().setCompletedEncountersMask(instanceLock.getData().getCompletedEncountersMask() | 1 << updateEvent.completedEncounter.bit);

            Log.outDebug(LogFilter.instance, String.format("[%1$s-%2$s | ", entries.Map.id, entries.Map.MapName.get(global.getWorldMgr().getDefaultDbcLocale())) + String.format("%1$s-%2$s] ", entries.MapDifficulty.difficultyID, CliDB.DifficultyStorage.get(entries.MapDifficulty.difficultyID).name) + String.format("Instance lock for %1$s in instance %2$s gains completed encounter [%3$s-%4$s]", playerGuid, updateEvent.instanceId, updateEvent.completedEncounter.id, updateEvent.completedEncounter.name.charAt(global.getWorldMgr().getDefaultDbcLocale())));
        }

        // Synchronize map completed encounters into players completed encounters for UI
        if (!entries.MapDifficulty.IsUsingEncounterLocks()) {
            instanceLock.getData().setCompletedEncountersMask(instanceLock.getData().getCompletedEncountersMask() | updateEvent.instanceCompletedEncountersMask);
        }

        if (updateEvent.entranceWorldSafeLocId != null) {
            instanceLock.getData().setEntranceWorldSafeLocId(updateEvent.entranceWorldSafeLocId.intValue());
        }

        if (instanceLock.isExpired()) {
            instanceLock.setExpiryTime(getNextResetTime(entries));
            instanceLock.setExtended(false);

            Log.outDebug(LogFilter.instance, String.format("[%1$s-%2$s | ", entries.Map.id, entries.Map.MapName.get(global.getWorldMgr().getDefaultDbcLocale())) + String.format("%1$s-%2$s] Expired instance lock for %3$s in instance %4$s is now active", entries.MapDifficulty.difficultyID, CliDB.DifficultyStorage.get(entries.MapDifficulty.difficultyID).name, playerGuid, updateEvent.instanceId));
        }

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHARACTER_INSTANCE_LOCK);
        stmt.AddValue(0, playerGuid.getCounter());
        stmt.AddValue(1, entries.MapDifficulty.mapID);
        stmt.AddValue(2, entries.MapDifficulty.LockID);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHARACTER_INSTANCE_LOCK);
        stmt.AddValue(0, playerGuid.getCounter());
        stmt.AddValue(1, entries.MapDifficulty.mapID);
        stmt.AddValue(2, entries.MapDifficulty.LockID);
        stmt.AddValue(3, instanceLock.getInstanceId());
        stmt.AddValue(4, entries.MapDifficulty.difficultyID);
        stmt.AddValue(5, instanceLock.getData().getData());
        stmt.AddValue(6, instanceLock.getData().getCompletedEncountersMask());
        stmt.AddValue(7, instanceLock.getData().getEntranceWorldSafeLocId());
        stmt.AddValue(8, (long) time.DateTimeToUnixTime(instanceLock.getExpiryTime()));
        stmt.AddValue(9, instanceLock.isExtended() ? 1 : 0);
        trans.append(stmt);

        return instanceLock;
    }

    public final void updateSharedInstanceLock(SQLTransaction trans, InstanceLockUpdateEvent updateEvent) {
        var sharedData = instanceLockDataById.get(updateEvent.instanceId);
        sharedData.data = updateEvent.newData;
        sharedData.instanceId = updateEvent.instanceId;

        if (updateEvent.completedEncounter != null) {
            sharedData.completedEncountersMask |= 1 << updateEvent.completedEncounter.bit;
            Log.outDebug(LogFilter.instance, String.format("Instance %1$s gains completed encounter [%2$s-%3$s]", updateEvent.instanceId, updateEvent.completedEncounter.id, updateEvent.completedEncounter.name.charAt(global.getWorldMgr().getDefaultDbcLocale())));
        }

        if (updateEvent.entranceWorldSafeLocId != null) {
            sharedData.entranceWorldSafeLocId = updateEvent.entranceWorldSafeLocId.intValue();
        }

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_INSTANCE);
        stmt.AddValue(0, sharedData.instanceId);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.INS_INSTANCE);
        stmt.AddValue(0, sharedData.instanceId);
        stmt.AddValue(1, sharedData.data);
        stmt.AddValue(2, sharedData.completedEncountersMask);
        stmt.AddValue(3, sharedData.entranceWorldSafeLocId);
        trans.append(stmt);
    }

    public final void onSharedInstanceLockDataDelete(int instanceId) {
        if (unloading) {
            return;
        }

        instanceLockDataById.remove(instanceId);
        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_INSTANCE);
        stmt.AddValue(0, instanceId);
        DB.characters.execute(stmt);
        Log.outDebug(LogFilter.instance, String.format("Deleting instance %1$s as it is no longer referenced by any player", instanceId));
    }

    public final Tuple<LocalDateTime, LocalDateTime> updateInstanceLockExtensionForPlayer(ObjectGuid playerGuid, MapDb2Entries entries, boolean extended) {
        var instanceLock = findActiveInstanceLock(playerGuid, entries, true, false);

        if (instanceLock != null) {
            var oldExpiryTime = instanceLock.getEffectiveExpiryTime();
            instanceLock.setExtended(extended);
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHARACTER_INSTANCE_LOCK_EXTENSION);
            stmt.AddValue(0, extended ? 1 : 0);
            stmt.AddValue(1, playerGuid.getCounter());
            stmt.AddValue(2, entries.MapDifficulty.mapID);
            stmt.AddValue(3, entries.MapDifficulty.LockID);
            DB.characters.execute(stmt);

            Log.outDebug(LogFilter.instance, String.format("[%1$s-%2$s | ", entries.Map.id, entries.Map.MapName.get(global.getWorldMgr().getDefaultDbcLocale())) + String.format("%1$s-%2$s] Instance lock for %3$s is %4$s extended", entries.MapDifficulty.difficultyID, CliDB.DifficultyStorage.get(entries.MapDifficulty.difficultyID).name, playerGuid, (extended ? "now" : "no longer")));

            return Tuple.create(oldExpiryTime, instanceLock.getEffectiveExpiryTime());
        }

        return Tuple.create(LocalDateTime.MIN, LocalDateTime.MIN);
    }

    /**
     * Resets instances that match given filter - for use in GM commands
     *
     * @param playerGuid         Guid of player whose locks will be removed
     * @param mapId              (Optional) Map id of instance locks to reset
     * @param difficulty         (Optional) Difficulty of instance locks to reset
     * @param locksReset         All locks that were reset
     * @param locksFailedToReset Locks that could not be reset because they are used by existing instance map
     */
    public final void resetInstanceLocksForPlayer(ObjectGuid playerGuid, Integer mapId, Difficulty difficulty, ArrayList<InstanceLock> locksReset, ArrayList<InstanceLock> locksFailedToReset) {
        var playerLocks = instanceLocksByPlayer.get(playerGuid);

        if (playerLocks == null) {
            return;
        }

        for (var playerLockPair : playerLocks) {
            if (playerLockPair.value.isInUse()) {
                locksFailedToReset.add(playerLockPair.value);

                continue;
            }

            if (mapId != null && mapId.intValue() != playerLockPair.value.getMapId()) {
                continue;
            }

            if (difficulty != null && difficulty != playerLockPair.value.getDifficultyId()) {
                continue;
            }

            locksReset.add(playerLockPair.value);
        }

        if (!locksReset.isEmpty()) {
            SQLTransaction trans = new SQLTransaction();

            for (var instanceLock : locksReset) {
                MapDb2Entries entries = new MapDb2Entries(instanceLock.getMapId(), instanceLock.getDifficultyId());
                var newExpiryTime = getNextResetTime(entries) - duration.FromSeconds(entries.MapDifficulty.GetRaidDuration());
                // set reset time to last reset time
                instanceLock.setExpiryTime(newExpiryTime);
                instanceLock.setExtended(false);

                var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHARACTER_INSTANCE_LOCK_FORCE_EXPIRE);
                stmt.AddValue(0, (long) time.DateTimeToUnixTime(newExpiryTime));
                stmt.AddValue(1, playerGuid.getCounter());
                stmt.AddValue(2, entries.MapDifficulty.mapID);
                stmt.AddValue(3, entries.MapDifficulty.LockID);
                trans.append(stmt);
            }

            DB.characters.CommitTransaction(trans);
        }
    }

    /**
     * Retrieves instance lock statistics - for use in GM commands
     *
     * @return Statistics info
     */
    public final InstanceLocksStatistics getStatistics() {
        InstanceLocksStatistics statistics = new InstanceLocksStatistics();
        statistics.instanceCount = instanceLockDataById.size();
        statistics.playerCount = instanceLocksByPlayer.size();

        return statistics;
    }

    public final LocalDateTime getNextResetTime(MapDb2Entries entries) {
        var dateTime = gameTime.GetDateAndTime();
        var resetHour = WorldConfig.getIntValue(WorldCfg.ResetScheduleHour);

        var hour = 0;
        var day = 0;

        switch (entries.MapDifficulty.ResetInterval) {
            case Daily: {
                if (dateTime.getHour() >= resetHour) {
                    day++;
                }

                hour = resetHour;

                break;
            }
            case Weekly: {
                var resetDay = WorldConfig.getIntValue(WorldCfg.ResetScheduleWeekDay);
                var daysAdjust = resetDay - dateTime.getDayOfMonth();

                if (dateTime.getDayOfMonth() > resetDay || (dateTime.getDayOfMonth() == resetDay && dateTime.getHour() >= resetHour)) {
                    daysAdjust += 7; // passed it for current week, grab time from next week
                }

                hour = resetHour;
                day += daysAdjust;

                break;
            }
            default:
                break;
        }

        return LocalDateTime.of(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth() + day, hour, 0, 0);
    }
}
