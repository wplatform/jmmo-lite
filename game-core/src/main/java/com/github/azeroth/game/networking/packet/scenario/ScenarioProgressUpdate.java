package com.github.azeroth.game.networking.packet.scenario;


import com.github.azeroth.game.networking.ServerPacket;

public class ScenarioProgressUpdate extends ServerPacket {
    public criteriaProgressPkt criteriaProgress = new criteriaProgressPkt();

    public ScenarioProgressUpdate() {
        super(ServerOpcode.ScenarioProgressUpdate);
    }

    @Override
    public void write() {
        criteriaProgress.write(this);
    }
}
