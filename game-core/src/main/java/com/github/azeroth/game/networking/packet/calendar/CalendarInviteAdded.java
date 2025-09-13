package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ServerPacket;

public class CalendarInviteAdded extends ServerPacket {
    public long inviteID;
    public long responseTime;
    public byte level = 100;
    public ObjectGuid inviteGuid = ObjectGuid.EMPTY;
    public long eventID;
    public byte type;
    public boolean clearPending;
    public CalendarInvitestatus status = CalendarInviteStatus.values()[0];

    public CalendarInviteAdded() {
        super(ServerOpcode.CalendarInviteAdded);
    }

    @Override
    public void write() {
        this.writeGuid(inviteGuid);
        this.writeInt64(eventID);
        this.writeInt64(inviteID);
        this.writeInt8(level);
        this.writeInt8((byte) status.getValue());
        this.writeInt8(type);
        this.writePackedTime(responseTime);

        this.writeBit(clearPending);
        this.flushBits();
    }
}
