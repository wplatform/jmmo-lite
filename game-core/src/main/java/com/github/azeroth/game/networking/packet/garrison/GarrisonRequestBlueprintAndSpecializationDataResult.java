package com.github.azeroth.game.networking.packet.garrison;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class GarrisonRequestBlueprintAndSpecializationDataResult extends ServerPacket {
    public GarrisonType garrTypeID = GarrisonType.values()[0];
    public ArrayList<Integer> specializationsKnown = null;
    public ArrayList<Integer> blueprintsKnown = null;

    public GarrisonRequestBlueprintAndSpecializationDataResult() {
        super(ServerOpcode.GarrisonRequestBlueprintAndSpecializationDataResult);
    }

    @Override
    public void write() {
        this.writeInt32((int) garrTypeID.getValue());
        this.writeInt32(blueprintsKnown != null ? blueprintsKnown.size() : 0);
        this.writeInt32(specializationsKnown != null ? specializationsKnown.size() : 0);

        if (blueprintsKnown != null) {
            for (var blueprint : blueprintsKnown) {
                this.writeInt32(blueprint);
            }
        }

        if (specializationsKnown != null) {
            for (var specialization : specializationsKnown) {
                this.writeInt32(specialization);
            }
        }
    }
}
