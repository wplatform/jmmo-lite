package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ServerPacket;

public class CalendarEventUpdatedAlert extends ServerPacket {
    public long eventID;
    public long date;
    public Calendarflags flags = CalendarFlags.values()[0];
    public long lockDate;
    public long originalDate;
    public int textureID;
    public CalendareventType eventType = CalendarEventType.values()[0];
    public boolean clearPending;
    public String description;
    public String eventName;

    public CalendarEventUpdatedAlert() {
        super(ServerOpcode.CalendarEventUpdatedAlert);
    }

    @Override
    public void write() {
        this.writeInt64(eventID);

        this.writePackedTime(originalDate);
        this.writePackedTime(date);
        this.writeInt32((int) lockDate);
        this.writeInt32((int) flags.getValue());
        this.writeInt32(textureID);
        this.writeInt8((byte) eventType.getValue());

        this.writeBits(eventName.getBytes().length, 8);
        this.writeBits(description.getBytes().length, 11);
        this.writeBit(clearPending);
        this.flushBits();

        this.writeString(eventName);
        this.writeString(description);
    }
}
