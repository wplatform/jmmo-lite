package com.github.azeroth.game.networking.packet.calendar;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class CalendarSendEvent extends ServerPacket {
    public ObjectGuid ownerGuid = ObjectGuid.EMPTY;
    public ObjectGuid eventGuildID = ObjectGuid.EMPTY;
    public long eventID;
    public long date;
    public long lockDate;
    public Calendarflags flags = CalendarFlags.values()[0];
    public int textureID;
    public CalendarEventType getEventType = CalendarEventType.values()[0];
    public CalendarSendeventType eventType = CalendarSendEventType.values()[0];
    public String description;
    public String eventName;
    public ArrayList<CalendarEventInviteInfo> invites = new ArrayList<>();

    public CalendarSendEvent() {
        super(ServerOpcode.CalendarSendEvent);
    }

    @Override
    public void write() {
        this.writeInt8((byte) eventType.getValue());
        this.writeGuid(ownerGuid);
        this.writeInt64(eventID);
        this.writeInt8((byte) getEventType.getValue());
        this.writeInt32(textureID);
        this.writeInt32((int) flags.getValue());
        this.writePackedTime(date);
        this.writeInt32((int) lockDate);
        this.writeGuid(eventGuildID);
        this.writeInt32(invites.size());

        this.writeBits(eventName.getBytes().length, 8);
        this.writeBits(description.getBytes().length, 11);
        this.flushBits();

        for (var invite : invites) {
            invite.write(this);
        }

        this.writeString(eventName);
        this.writeString(description);
    }
}
