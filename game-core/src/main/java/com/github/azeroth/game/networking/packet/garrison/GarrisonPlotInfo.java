package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.networking.WorldPacket;

public final class GarrisonPlotInfo {

    public int garrPlotInstanceID;
    public Position plotPos;

    public int plotType;

    public void write(WorldPacket data) {
        data.writeInt32(garrPlotInstanceID);
        data.writeXYZO(plotPos);
        data.writeInt32(plotType);
    }

    public GarrisonPlotInfo clone() {
        GarrisonPlotInfo varCopy = new garrisonPlotInfo();

        varCopy.garrPlotInstanceID = this.garrPlotInstanceID;
        varCopy.plotPos = this.plotPos;
        varCopy.plotType = this.plotType;

        return varCopy;
    }
}
