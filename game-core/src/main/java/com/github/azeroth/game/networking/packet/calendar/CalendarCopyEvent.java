package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CalendarCopyEvent extends ClientPacket {
    public long moderatorID;
    public long eventID;
    public long eventClubID;
    public long date;

    public CalendarCopyEvent(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        eventID = this.readUInt64();
        moderatorID = this.readUInt64();
        eventClubID = this.readUInt64();
        date = this.readPackedTime();
    }
}
