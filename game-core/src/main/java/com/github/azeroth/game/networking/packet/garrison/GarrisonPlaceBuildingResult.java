package com.github.azeroth.game.networking.packet.garrison;


import com.github.azeroth.game.networking.ServerPacket;

public class GarrisonPlaceBuildingResult extends ServerPacket {
    public GarrisonType garrTypeID = GarrisonType.values()[0];
    public GarrisonError result = GarrisonError.values()[0];
    public GarrisonbuildingInfo buildingInfo = new garrisonBuildingInfo();
    public boolean playActivationCinematic;

    public GarrisonPlaceBuildingResult() {
        super(ServerOpcode.GarrisonPlaceBuildingResult);
    }

    @Override
    public void write() {
        this.writeInt32(garrTypeID.getValue());
        this.writeInt32((int) result.getValue());
        buildingInfo.write(this);
        this.writeBit(playActivationCinematic);
        this.flushBits();
    }
}
