package com.github.azeroth.game.networking.packet.calendar;


import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class HandleCalendarRsvp extends ClientPacket {
    public long inviteID;
    public long eventID;
    public CalendarInvitestatus status = CalendarInviteStatus.values()[0];

    public handleCalendarRsvp(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        eventID = this.readUInt64();
        inviteID = this.readUInt64();
        status = CalendarInviteStatus.forValue(this.readUInt8());
    }
}
