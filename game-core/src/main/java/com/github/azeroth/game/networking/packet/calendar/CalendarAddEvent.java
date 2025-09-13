package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CalendarAddEvent extends ClientPacket {
    public int maxSize = 100;
    public CalendarAddeventInfo eventInfo = new calendarAddEventInfo();

    public CalendarAddEvent(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        eventInfo.read(this);
        maxSize = this.readUInt32();
    }
}
