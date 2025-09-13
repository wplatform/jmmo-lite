package com.github.azeroth.game.networking.packet.calendar;


import com.github.azeroth.game.networking.WorldPacket;

final class CalendarSendCalendarEventInfo {
    public ObjectGuid ownerGuid = ObjectGuid.EMPTY;

    public long eventID;
    public String eventName;
    public CalendareventType eventType = CalendarEventType.values()[0];
    public long date;
    public Calendarflags flags = CalendarFlags.values()[0];
    public int textureID;
    public long eventClubID;

    public void write(WorldPacket data) {
        data.writeInt64(eventID);
        data.writeInt8((byte) eventType.getValue());
        data.writePackedTime(date);
        data.writeInt32((int) flags.getValue());
        data.writeInt32(textureID);
        data.writeInt64(eventClubID);
        data.writeGuid(ownerGuid);

        data.writeBits(eventName.getBytes().length, 8);
        data.flushBits();
        data.writeString(eventName);
    }

    public CalendarSendCalendarEventInfo clone() {
        CalendarSendCalendarEventInfo varCopy = new CalendarSendCalendarEventInfo();

        varCopy.eventID = this.eventID;
        varCopy.eventName = this.eventName;
        varCopy.eventType = this.eventType;
        varCopy.date = this.date;
        varCopy.flags = this.flags;
        varCopy.textureID = this.textureID;
        varCopy.eventClubID = this.eventClubID;
        varCopy.ownerGuid = this.ownerGuid;

        return varCopy;
    }
}
