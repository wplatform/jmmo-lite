package com.github.azeroth.game.map;


import java.time.LocalDateTime;


class SharedInstanceLock extends InstanceLock {
    /**
     * Instance id based locks have two states
     * One shared by everyone, which is the real state used by instance
     * and one for each player that shows in UI that might have less encounters completed
     */
    private final SharedInstanceLockData sharedData;

    public SharedInstanceLock(int mapId, Difficulty difficultyId, LocalDateTime expiryTime, int instanceId, SharedInstanceLockData sharedData) {
        super(mapId, difficultyId, expiryTime, instanceId);
        sharedData = sharedData;
    }

    @Override
    public InstanceLockData getInstanceInitializationData() {
        return sharedData;
    }

    public final SharedInstanceLockData getSharedData() {
        return sharedData;
    }
}
