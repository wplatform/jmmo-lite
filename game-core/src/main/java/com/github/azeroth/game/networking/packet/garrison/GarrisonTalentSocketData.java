package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.WorldPacket;

final class GarrisonTalentSocketData {
    public int soulbindConduitID;
    public int soulbindConduitRank;

    public void write(WorldPacket data) {
        data.writeInt32(soulbindConduitID);
        data.writeInt32(soulbindConduitRank);
    }

    public GarrisonTalentSocketData clone() {
        GarrisonTalentSocketData varCopy = new GarrisonTalentSocketData();

        varCopy.soulbindConduitID = this.soulbindConduitID;
        varCopy.soulbindConduitRank = this.soulbindConduitRank;

        return varCopy;
    }
}
