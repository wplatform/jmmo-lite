package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.WorldPacket;

final class CalendarUpdateEventInfo {
    public long clubID;
    public long eventID;
    public long moderatorID;
    public String title;
    public String description;
    public byte eventType;
    public int textureID;
    public long time;
    public int flags;

    public void read(WorldPacket data) {
        clubID = data.readUInt64();
        eventID = data.readUInt64();
        moderatorID = data.readUInt64();
        eventType = data.readUInt8();
        textureID = data.readUInt32();
        time = data.readPackedTime();
        flags = data.readUInt32();

        var titleLen = data.<Byte>readBit(8);
        var descLen = data.<SHORT>readBit(11);

        title = data.readString(titleLen);
        description = data.readString(descLen);
    }

    public CalendarUpdateEventInfo clone() {
        CalendarUpdateEventInfo varCopy = new calendarUpdateEventInfo();

        varCopy.clubID = this.clubID;
        varCopy.eventID = this.eventID;
        varCopy.moderatorID = this.moderatorID;
        varCopy.title = this.title;
        varCopy.description = this.description;
        varCopy.eventType = this.eventType;
        varCopy.textureID = this.textureID;
        varCopy.time = this.time;
        varCopy.flags = this.flags;

        return varCopy;
    }
}
