package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.WorldPacket;

class GarrisonCancelConstruction extends ClientPacket {
    public ObjectGuid npcGUID = ObjectGuid.EMPTY;
    public int plotInstanceID;

    public GarrisonCancelConstruction(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        npcGUID = this.readPackedGuid();
        plotInstanceID = this.readUInt();
    }
}
