package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ServerPacket;

public class CalendarInviteRemoved extends ServerPacket {
    public ObjectGuid inviteGuid = ObjectGuid.EMPTY;
    public long eventID;
    public int flags;
    public boolean clearPending;

    public CalendarInviteRemoved() {
        super(ServerOpcode.CalendarInviteRemoved);
    }

    @Override
    public void write() {
        this.writeGuid(inviteGuid);
        this.writeInt64(eventID);
        this.writeInt32(flags);

        this.writeBit(clearPending);
        this.flushBits();
    }
}
