package com.github.azeroth.game.networking.packet.calendar;


import com.github.azeroth.game.networking.ServerPacket;

public class CalendarRaidLockoutRemoved extends ServerPacket {

    public long instanceID;
    public int mapID;
    public Difficulty difficultyID = Difficulty.values()[0];

    public CalendarRaidLockoutRemoved() {
        super(ServerOpcode.CalendarRaidLockoutRemoved);
    }

    @Override
    public void write() {
        this.writeInt64(instanceID);
        this.writeInt32(mapID);
        this.writeInt32((int) difficultyID.getValue());
    }
}
