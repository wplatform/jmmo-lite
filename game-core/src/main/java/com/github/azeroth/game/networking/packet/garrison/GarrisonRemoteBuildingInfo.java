package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.WorldPacket;

final class GarrisonRemoteBuildingInfo {
    public int garrPlotInstanceID;
    public int garrBuildingID;

    public GarrisonRemoteBuildingInfo() {
    }

    public GarrisonRemoteBuildingInfo(int plotInstanceId, int buildingId) {
        garrPlotInstanceID = plotInstanceId;
        garrBuildingID = buildingId;
    }

    public void write(WorldPacket data) {
        data.writeInt32(garrPlotInstanceID);
        data.writeInt32(garrBuildingID);
    }

    public GarrisonRemoteBuildingInfo clone() {
        GarrisonRemoteBuildingInfo varCopy = new GarrisonRemoteBuildingInfo();

        varCopy.garrPlotInstanceID = this.garrPlotInstanceID;
        varCopy.garrBuildingID = this.garrBuildingID;

        return varCopy;
    }
}
