package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ServerPacket;

public class CalendarRaidLockoutAdded extends ServerPacket {
    public long instanceID;
    public Difficulty difficultyID = Difficulty.values()[0];
    public int timeRemaining;
    public int serverTime;
    public int mapID;

    public CalendarRaidLockoutAdded() {
        super(ServerOpcode.CalendarRaidLockoutAdded);
    }

    @Override
    public void write() {
        this.writeInt64(instanceID);
        this.writeInt32(serverTime);
        this.writeInt32(mapID);
        this.writeInt32((int) difficultyID.getValue());
        this.writeInt32(timeRemaining);
    }
}
