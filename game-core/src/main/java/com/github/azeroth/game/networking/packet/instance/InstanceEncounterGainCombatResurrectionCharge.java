package com.github.azeroth.game.networking.packet.instance;

import com.github.azeroth.game.networking.ServerPacket;

public class InstanceEncounterGainCombatResurrectionCharge extends ServerPacket {
    public int inCombatResCount;
    public int combatResChargeRecovery;

    public InstanceEncounterGainCombatResurrectionCharge() {
        super(ServerOpcode.InstanceEncounterGainCombatResurrectionCharge);
    }

    @Override
    public void write() {
        this.writeInt32(inCombatResCount);
        this.writeInt32(combatResChargeRecovery);
    }
}
