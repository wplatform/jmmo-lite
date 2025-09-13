package com.github.azeroth.game.map;

public final class InstanceLocksStatistics {
    public int instanceCount; // Number of existing ID-based locks
    public int playerCount; // Number of players that have any lock

    public InstanceLocksStatistics clone() {
        InstanceLocksStatistics varCopy = new InstanceLocksStatistics();

        varCopy.instanceCount = this.instanceCount;
        varCopy.playerCount = this.playerCount;

        return varCopy;
    }
}
