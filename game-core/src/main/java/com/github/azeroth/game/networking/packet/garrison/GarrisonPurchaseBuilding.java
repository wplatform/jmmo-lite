package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.entity.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class GarrisonPurchaseBuilding extends ClientPacket {
    public ObjectGuid npcGUID = ObjectGuid.EMPTY;

    public int buildingID;

    public int plotInstanceID;

    public GarrisonPurchaseBuilding(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        npcGUID = this.readPackedGuid();
        plotInstanceID = this.readUInt32();
        buildingID = this.readUInt32();
    }
}
