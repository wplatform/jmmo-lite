package com.github.azeroth.game.networking.packet.instance;

import com.github.azeroth.game.networking.ServerPacket;

public class InstanceEncounterInCombatResurrection extends ServerPacket {
    public InstanceEncounterInCombatResurrection() {
        super(ServerOpcode.InstanceEncounterInCombatResurrection);
    }

    @Override
    public void write() {
    }
}
