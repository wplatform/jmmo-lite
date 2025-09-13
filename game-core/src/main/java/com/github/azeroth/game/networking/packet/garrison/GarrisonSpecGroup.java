package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.WorldPacket;

final class GarrisonSpecGroup {
    public int chrSpecializationID;
    public int soulbindID;

    public void write(WorldPacket data) {
        data.writeInt32(chrSpecializationID);
        data.writeInt32(soulbindID);
    }

    public GarrisonSpecGroup clone() {
        GarrisonSpecGroup varCopy = new GarrisonSpecGroup();

        varCopy.chrSpecializationID = this.chrSpecializationID;
        varCopy.soulbindID = this.soulbindID;

        return varCopy;
    }
}
