package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.WorldPacket;

final class GarrisonEventEntry {
    public int entryID;
    public long eventValue;

    public void write(WorldPacket data) {
        data.writeInt64(eventValue);
        data.writeInt32(entryID);
    }

    public GarrisonEventEntry clone() {
        GarrisonEventEntry varCopy = new GarrisonEventEntry();

        varCopy.entryID = this.entryID;
        varCopy.eventValue = this.eventValue;

        return varCopy;
    }
}
