package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ServerPacket;

public class CalendarInviteStatusPacket extends ServerPacket {
    public Calendarflags flags = CalendarFlags.values()[0];
    public long eventID;
    public CalendarInvitestatus status = CalendarInviteStatus.values()[0];
    public boolean clearPending;
    public long responseTime;
    public long date;
    public ObjectGuid inviteGuid = ObjectGuid.EMPTY;

    public CalendarInviteStatusPacket() {
        super(ServerOpcode.CalendarInviteStatus);
    }

    @Override
    public void write() {
        this.writeGuid(inviteGuid);
        this.writeInt64(eventID);
        this.writePackedTime(date);
        this.writeInt32((int) flags.getValue());
        this.writeInt8((byte) status.getValue());
        this.writePackedTime(responseTime);

        this.writeBit(clearPending);
        this.flushBits();
    }
}
