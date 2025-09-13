package com.github.azeroth.game.map;


import Time.GameTime;
import com.github.azeroth.dbc.defines.Difficulty;
import com.github.azeroth.game.domain.map.MapDb2Entries;

import java.time.LocalDateTime;

public class InstanceLock {

    private final int mapId;
    private final Difficulty difficultyId;
    private final InstanceLockData data = new InstanceLockData();

    private int instanceId;
    private LocalDateTime expiryTime = LocalDateTime.MIN;
    private boolean extended;
    private boolean isInUse;


    public InstanceLock(int mapId, Difficulty difficultyId, LocalDateTime expiryTime, int instanceId) {
        this.mapId = mapId;
        this.difficultyId = difficultyId;
        this.instanceId = instanceId;
        this.expiryTime = expiryTime;
        this.extended = false;
    }

    public final boolean isExpired() {
        return expiryTime.compareTo(GameTime.getSystemTime()) < 0;
    }

    public final LocalDateTime getEffectiveExpiryTime() {
        if (!isExtended()) {
            return getExpiryTime();
        }

        MapDb2Entries entries = new MapDb2Entries(mapId, difficultyId);

        // return next reset time
        if (isExpired()) {
            return global.getInstanceLockMgr().getNextResetTime(entries);
        }

        // if not expired, return expiration time + 1 reset period
        return getExpiryTime() + duration.FromSeconds(entries.MapDifficulty.GetRaidDuration());
    }


    public final int getMapId() {
        return mapId;
    }

    public final Difficulty getDifficultyId() {
        return difficultyId;
    }


    public final int getInstanceId() {
        return instanceId;
    }


    public final void setInstanceId(int instanceId) {
        instanceId = instanceId;
    }

    public final LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public final void setExpiryTime(LocalDateTime expiryTime) {
        expiryTime = expiryTime;
    }

    public final boolean isExtended() {
        return extended;
    }

    public final void setExtended(boolean extended) {
        extended = extended;
    }

    public final InstanceLockData getData() {
        return data;
    }

    public InstanceLockData getInstanceInitializationData() {
        return data;
    }

    public final boolean isInUse() {
        return isInUse;
    }

    public final void setInUse(boolean inUse) {
        isInUse = inUse;
    }
}
