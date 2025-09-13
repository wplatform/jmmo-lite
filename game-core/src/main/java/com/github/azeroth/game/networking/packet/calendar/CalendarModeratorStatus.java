package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ServerPacket;

public class CalendarModeratorStatus extends ServerPacket {
    public ObjectGuid inviteGuid = ObjectGuid.EMPTY;
    public long eventID;
    public CalendarInvitestatus status = CalendarInviteStatus.values()[0];
    public boolean clearPending;

    public CalendarModeratorStatus() {
        super(ServerOpcode.CalendarModeratorStatus);
    }

    @Override
    public void write() {
        this.writeGuid(inviteGuid);
        this.writeInt64(eventID);
        this.writeInt8((byte) status.getValue());

        this.writeBit(clearPending);
        this.flushBits();
    }
}
