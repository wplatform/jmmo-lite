package com.github.azeroth.game.networking.packet.calendar;


import com.github.azeroth.game.networking.ServerPacket;

public class CalendarEventRemovedAlert extends ServerPacket {
    public long eventID;
    public long date;
    public boolean clearPending;

    public CalendarEventRemovedAlert() {
        super(ServerOpcode.CalendarEventRemovedAlert);
    }

    @Override
    public void write() {
        this.writeInt64(eventID);
        this.writePackedTime(date);

        this.writeBit(clearPending);
        this.flushBits();
    }
}
