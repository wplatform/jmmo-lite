package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ServerPacket;

public class CalendarInviteNotes extends ServerPacket {
    public ObjectGuid inviteGuid = ObjectGuid.EMPTY;
    public long eventID;
    public String notes = "";
    public boolean clearPending;

    public CalendarInviteNotes() {
        super(ServerOpcode.CalendarInviteNotes);
    }

    @Override
    public void write() {
        this.writeGuid(inviteGuid);
        this.writeInt64(eventID);

        this.writeBits(notes.getBytes().length, 8);
        this.writeBit(clearPending);
        this.flushBits();
        this.writeString(notes);
    }
}
