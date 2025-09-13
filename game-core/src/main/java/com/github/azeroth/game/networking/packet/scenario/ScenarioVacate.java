package com.github.azeroth.game.networking.packet.scenario;

import com.github.azeroth.game.networking.ServerPacket;

public class ScenarioVacate extends ServerPacket {
    public int scenarioID;
    public int unk1;
    public byte unk2;

    public ScenarioVacate() {
        super(ServerOpcode.ScenarioVacate);
    }

    @Override
    public void write() {
        this.writeInt32(scenarioID);
        this.writeInt32(unk1);
        this.writeBits(unk2, 2);
        this.flushBits();
    }
}
