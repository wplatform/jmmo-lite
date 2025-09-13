package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CalendarUpdateEvent extends ClientPacket {
    public int maxSize;
    public CalendarUpdateeventInfo eventInfo = new calendarUpdateEventInfo();

    public CalendarUpdateEvent(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        eventInfo.read(this);
        maxSize = this.readUInt32();
    }
}
