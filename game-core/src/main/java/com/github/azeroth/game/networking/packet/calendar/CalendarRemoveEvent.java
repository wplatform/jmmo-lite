package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CalendarRemoveEvent extends ClientPacket {
    public long moderatorID;
    public long eventID;
    public long clubID;
    public int flags;

    public CalendarRemoveEvent(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        eventID = this.readUInt64();
        moderatorID = this.readUInt64();
        clubID = this.readUInt64();
        flags = this.readUInt32();
    }
}
