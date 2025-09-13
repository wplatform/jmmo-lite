package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.WorldPacket;

final class CalendarSendCalendarRaidLockoutInfo {
    public long instanceID;
    public int mapID;
    public int difficultyID;
    public int expireTime;

    public void write(WorldPacket data) {
        data.writeInt64(instanceID);
        data.writeInt32(mapID);
        data.writeInt32(difficultyID);
        data.writeInt32(expireTime);
    }

    public CalendarSendCalendarRaidLockoutInfo clone() {
        CalendarSendCalendarRaidLockoutInfo varCopy = new CalendarSendCalendarRaidLockoutInfo();

        varCopy.instanceID = this.instanceID;
        varCopy.mapID = this.mapID;
        varCopy.difficultyID = this.difficultyID;
        varCopy.expireTime = this.expireTime;

        return varCopy;
    }
}
