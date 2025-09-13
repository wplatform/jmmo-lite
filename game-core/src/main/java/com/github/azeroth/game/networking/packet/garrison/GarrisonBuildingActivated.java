package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.ServerPacket;

public class GarrisonBuildingActivated extends ServerPacket {
    public int garrPlotInstanceID;

    public GarrisonBuildingActivated() {
        super(ServerOpcode.GarrisonBuildingActivated);
    }

    @Override
    public void write() {
        this.writeInt32(garrPlotInstanceID);
    }
}
