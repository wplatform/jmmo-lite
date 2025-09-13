package com.github.azeroth.game.networking.packet.calendar;


import com.github.azeroth.game.networking.WorldPacket;

class CalendarAddEventInfo {
    public long clubId;
    public String title;
    public String description;
    public byte eventType;
    public int textureID;
    public long time;
    public int flags;
    public CalendarAddEventInviteInfo[] invites = new CalendarAddEventInviteInfo[(int) SharedConst.CalendarMaxInvites];

    public final void read(WorldPacket data) {
        clubId = data.readUInt64();
        eventType = data.readUInt8();
        textureID = data.readInt32();
        time = data.readPackedTime();
        flags = data.readUInt32();
        var InviteCount = data.readUInt32();

        var titleLength = data.<Byte>readBit(8);
        var descriptionLength = data.<SHORT>readBit(11);

        for (var i = 0; i < InviteCount; ++i) {
            CalendarAddEventInviteInfo invite = new CalendarAddEventInviteInfo();
            invite.read(data);
            Invites[i] = invite;
        }

        title = data.readString(titleLength);
        description = data.readString(descriptionLength);
    }
}
