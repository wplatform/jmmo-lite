package com.github.azeroth.game.networking.packet.instance;

import com.github.azeroth.game.networking.ServerPacket;

public class InstanceEncounterStart extends ServerPacket {
    public int inCombatResCount; // amount of usable battle ressurections
    public int maxInCombatResCount;
    public int combatResChargeRecovery;
    public int nextCombatResChargeTime;
    public boolean inProgress = true;

    public InstanceEncounterStart() {
        super(ServerOpcode.InstanceEncounterStart);
    }

    @Override
    public void write() {
        this.writeInt32(inCombatResCount);
        this.writeInt32(maxInCombatResCount);
        this.writeInt32(combatResChargeRecovery);
        this.writeInt32(nextCombatResChargeTime);
        this.writeBit(inProgress);
        this.flushBits();
    }
}
