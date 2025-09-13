package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CalendarGetEvent extends ClientPacket {
    public long eventID;

    public CalendarGetEvent(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        eventID = this.readUInt64();
    }
}
