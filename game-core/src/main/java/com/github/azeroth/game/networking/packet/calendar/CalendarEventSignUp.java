package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CalendarEventSignUp extends ClientPacket {
    public boolean tentative;
    public long eventID;
    public long clubID;

    public CalendarEventSignUp(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        eventID = this.readUInt64();
        clubID = this.readUInt64();
        tentative = this.readBit();
    }
}
