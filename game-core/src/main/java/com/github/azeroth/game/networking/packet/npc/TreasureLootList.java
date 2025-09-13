package com.github.azeroth.game.networking.packet.npc;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class TreasureLootList {
    public ArrayList<TreasureItem> items = new ArrayList<>();

    public final void write(WorldPacket data) {
        data.writeInt32(items.size());

        for (var treasureItem : items) {
            treasureItem.write(data);
        }
    }
}
