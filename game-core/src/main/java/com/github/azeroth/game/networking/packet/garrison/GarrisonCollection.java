package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


class GarrisonCollection {
    public int type;
    public ArrayList<GarrisonCollectionEntry> entries = new ArrayList<>();

    public final void write(WorldPacket data) {
        data.writeInt32(type);
        data.writeInt32(entries.size());

        for (var collectionEntry : entries) {
            collectionEntry.write(data);
        }
    }
}
