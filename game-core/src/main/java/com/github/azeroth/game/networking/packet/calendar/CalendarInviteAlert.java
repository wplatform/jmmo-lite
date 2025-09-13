package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ServerPacket;

public class CalendarInviteAlert extends ServerPacket {
    public ObjectGuid ownerGuid = ObjectGuid.EMPTY;
    public ObjectGuid eventGuildID = ObjectGuid.EMPTY;
    public ObjectGuid invitedByGuid = ObjectGuid.EMPTY;
    public long inviteID;
    public long eventID;
    public Calendarflags flags = CalendarFlags.values()[0];
    public long date;
    public int textureID;
    public CalendarInvitestatus status = CalendarInviteStatus.values()[0];
    public CalendareventType eventType = CalendarEventType.values()[0];
    public CalendarModerationRank moderatorStatus = CalendarModerationRank.values()[0];
    public String eventName;

    public CalendarInviteAlert() {
        super(ServerOpcode.CalendarInviteAlert);
    }

    @Override
    public void write() {
        this.writeInt64(eventID);
        this.writePackedTime(date);
        this.writeInt32((int) flags.getValue());
        this.writeInt8((byte) eventType.getValue());
        this.writeInt32(textureID);
        this.writeGuid(eventGuildID);
        this.writeInt64(inviteID);
        this.writeInt8((byte) status.getValue());
        this.writeInt8((byte) moderatorStatus.getValue());

        // Todo: check order
        this.writeGuid(invitedByGuid);
        this.writeGuid(ownerGuid);

        this.writeBits(eventName.getBytes().length, 8);
        this.flushBits();
        this.writeString(eventName);
    }
}
