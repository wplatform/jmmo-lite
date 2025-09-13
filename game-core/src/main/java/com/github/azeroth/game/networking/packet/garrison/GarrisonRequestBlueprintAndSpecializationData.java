package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class GarrisonRequestBlueprintAndSpecializationData extends ClientPacket {
    public GarrisonRequestBlueprintAndSpecializationData(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
