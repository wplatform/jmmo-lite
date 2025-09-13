package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.WorldPacket;

final class GarrisonCollectionEntry {
    public int entryID;
    public int rank;

    public void write(WorldPacket data) {
        data.writeInt32(entryID);
        data.writeInt32(rank);
    }

    public GarrisonCollectionEntry clone() {
        GarrisonCollectionEntry varCopy = new GarrisonCollectionEntry();

        varCopy.entryID = this.entryID;
        varCopy.rank = this.rank;

        return varCopy;
    }
}
