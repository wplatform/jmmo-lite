package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ServerPacket;

public class CalendarInviteNotesAlert extends ServerPacket {
    public long eventID;
    public String notes;

    public CalendarInviteNotesAlert(long eventID, String notes) {
        super(ServerOpcode.CalendarInviteNotesAlert);
        eventID = eventID;
        notes = notes;
    }

    @Override
    public void write() {
        this.writeInt64(eventID);

        this.writeBits(notes.getBytes().length, 8);
        this.flushBits();
        this.writeString(notes);
    }
}
