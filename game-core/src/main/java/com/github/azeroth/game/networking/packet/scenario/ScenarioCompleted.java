package com.github.azeroth.game.networking.packet.scenario;

import com.github.azeroth.game.networking.ServerPacket;

public class ScenarioCompleted extends ServerPacket {
    public int scenarioID;

    public ScenarioCompleted(int scenarioId) {
        super(ServerOpcode.ScenarioCompleted);
        scenarioID = scenarioId;
    }

    @Override
    public void write() {
        this.writeInt32(scenarioID);
    }
}
