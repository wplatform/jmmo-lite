package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CalendarGetCalendar extends ClientPacket {
    public CalendarGetCalendar(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}

//Structs

