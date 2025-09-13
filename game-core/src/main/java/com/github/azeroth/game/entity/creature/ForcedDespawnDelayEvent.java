package com.github.azeroth.game.entity.creature;


import java.time.Duration;

public class ForcedDespawnDelayEvent extends BasicEvent {
    private final Creature owner;
    private final Duration respawnTimer;


    public ForcedDespawnDelayEvent(Creature owner) {
        this(owner, null);
    }

    public ForcedDespawnDelayEvent(Creature owner, Duration respawnTimer) {
        owner = owner;
        respawnTimer = respawnTimer;
    }

    @Override
    public boolean execute(long etime, int pTime) {
        owner.despawnOrUnsummon(Duration.ZERO, respawnTimer); // since we are here, we are not TempSummon as object type cannot change during runtime

        return true;
    }
}
