package com.github.azeroth.game.networking.packet.calendar;


import com.github.azeroth.game.networking.WorldPacket;


class CalendarEventInviteInfo {
    public ObjectGuid UUID = ObjectGuid.EMPTY;
    public long inviteID;
    public long responseTime;
    public byte level = 1;
    public CalendarInvitestatus status = CalendarInviteStatus.values()[0];
    public CalendarModerationRank moderator = CalendarModerationRank.values()[0];
    public byte inviteType;
    public String notes;

    public final void write(WorldPacket data) {
        data.writeGuid(UUID);
        data.writeInt64(inviteID);

        data.writeInt8(level);
        data.writeInt8((byte) status.getValue());
        data.writeInt8((byte) moderator.getValue());
        data.writeInt8(inviteType);

        data.writePackedTime(responseTime);

        data.writeBits(notes.getBytes().length, 8);
        data.flushBits();
        data.writeString(notes);
    }
}
