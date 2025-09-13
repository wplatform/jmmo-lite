package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ServerPacket;

public class CalendarInviteRemovedAlert extends ServerPacket {
    public long eventID;
    public long date;
    public Calendarflags flags = CalendarFlags.values()[0];
    public CalendarInvitestatus status = CalendarInviteStatus.values()[0];

    public CalendarInviteRemovedAlert() {
        super(ServerOpcode.CalendarInviteRemovedAlert);
    }

    @Override
    public void write() {
        this.writeInt64(eventID);
        this.writePackedTime(date);
        this.writeInt32((int) flags.getValue());
        this.writeInt8((byte) status.getValue());
    }
}
