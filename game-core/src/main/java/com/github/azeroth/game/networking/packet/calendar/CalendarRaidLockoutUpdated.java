package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ServerPacket;

public class CalendarRaidLockoutUpdated extends ServerPacket {
    public long serverTime;
    public int mapID;
    public int difficultyID;
    public int newTimeRemaining;
    public int oldTimeRemaining;

    public CalendarRaidLockoutUpdated() {
        super(ServerOpcode.CalendarRaidLockoutUpdated);
    }

    @Override
    public void write() {
        this.writePackedTime(serverTime);
        this.writeInt32(mapID);
        this.writeInt32(difficultyID);
        this.writeInt32(oldTimeRemaining);
        this.writeInt32(newTimeRemaining);
    }
}
