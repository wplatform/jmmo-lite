package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


class GarrisonRemoteSiteInfo {
    public int garrSiteLevelID;
    public ArrayList<GarrisonRemoteBuildingInfo> buildings = new ArrayList<>();

    public final void write(WorldPacket data) {
        data.writeInt32(garrSiteLevelID);
        data.writeInt32(buildings.size());

        for (var building : buildings) {
            building.write(data);
        }
    }
}
