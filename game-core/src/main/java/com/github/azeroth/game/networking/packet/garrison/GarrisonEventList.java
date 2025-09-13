package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


class GarrisonEventList {
    public int type;
    public ArrayList<GarrisonEventEntry> events = new ArrayList<>();

    public final void write(WorldPacket data) {
        data.writeInt32(type);
        data.writeInt32(events.size());

        for (var eventEntry : events) {
            eventEntry.write(data);
        }
    }
}
