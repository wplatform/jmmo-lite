package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.ServerPacket;

public class GarrisonPlotRemoved extends ServerPacket {
    public int garrPlotInstanceID;

    public GarrisonPlotRemoved() {
        super(ServerOpcode.GarrisonPlotRemoved);
    }

    @Override
    public void write() {
        this.writeInt32(garrPlotInstanceID);
    }
}
