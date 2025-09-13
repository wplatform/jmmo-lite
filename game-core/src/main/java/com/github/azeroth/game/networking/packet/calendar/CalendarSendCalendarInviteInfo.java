package com.github.azeroth.game.networking.packet.calendar;


import com.github.azeroth.game.networking.WorldPacket;

final class CalendarSendCalendarInviteInfo {

    public long eventID;

    public long inviteID;
    public ObjectGuid inviterGuid = ObjectGuid.EMPTY;
    public CalendarInvitestatus status = CalendarInviteStatus.values()[0];
    public CalendarModerationRank moderator = CalendarModerationRank.values()[0];

    public byte inviteType;

    public void write(WorldPacket data) {
        data.writeInt64(eventID);
        data.writeInt64(inviteID);
        data.writeInt8((byte) status.getValue());
        data.writeInt8((byte) moderator.getValue());
        data.writeInt8(inviteType);
        data.writeGuid(inviterGuid);
    }

    public CalendarSendCalendarInviteInfo clone() {
        CalendarSendCalendarInviteInfo varCopy = new CalendarSendCalendarInviteInfo();

        varCopy.eventID = this.eventID;
        varCopy.inviteID = this.inviteID;
        varCopy.inviterGuid = this.inviterGuid;
        varCopy.status = this.status;
        varCopy.moderator = this.moderator;
        varCopy.inviteType = this.inviteType;

        return varCopy;
    }
}
