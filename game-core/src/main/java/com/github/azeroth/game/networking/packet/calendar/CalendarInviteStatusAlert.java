package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ServerPacket;

public class CalendarInviteStatusAlert extends ServerPacket {
    public long eventID;
    public int flags;
    public long date;
    public byte status;

    public CalendarInviteStatusAlert() {
        super(ServerOpcode.CalendarInviteStatusAlert);
    }

    @Override
    public void write() {
        this.writeInt64(eventID);
        this.writePackedTime(date);
        this.writeInt32(flags);
        this.writeInt8(status);
    }
}
