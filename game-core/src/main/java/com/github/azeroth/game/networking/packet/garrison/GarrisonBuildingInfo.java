package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.WorldPacket;

public class GarrisonBuildingInfo {
    public int garrPlotInstanceID;
    public int garrBuildingID;
    public long timeBuilt;
    public int currentGarSpecID;
    public long timeSpecCooldown = 2288912640; // 06/07/1906 18:35:44 - another in the series of magic blizz dates
    public boolean active;

    public final void write(WorldPacket data) {
        data.writeInt32(garrPlotInstanceID);
        data.writeInt32(garrBuildingID);
        data.writeInt64(timeBuilt);
        data.writeInt32(currentGarSpecID);
        data.writeInt64(timeSpecCooldown);
        data.writeBit(active);
        data.flushBits();
    }
}
