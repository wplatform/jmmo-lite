package com.github.azeroth.game.networking.packet.garrison;


import com.github.azeroth.game.networking.ServerPacket;

public class GarrisonPlotPlaced extends ServerPacket {
    public GarrisonType garrTypeID = GarrisonType.values()[0];
    public GarrisonplotInfo plotInfo = new garrisonPlotInfo();

    public GarrisonPlotPlaced() {
        super(ServerOpcode.GarrisonPlotPlaced);
    }

    @Override
    public void write() {
        this.writeInt32(garrTypeID.getValue());
        plotInfo.write(this);
    }
}
