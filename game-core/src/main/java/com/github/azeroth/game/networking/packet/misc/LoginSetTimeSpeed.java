package com.github.azeroth.game.networking.packet.misc;


public class LoginSetTimeSpeed extends ServerPacket {
    public float newSpeed;
    public int serverTimeHolidayOffset;
    public int gameTime;
    public int serverTime;
    public int gameTimeHolidayOffset;

    public LoginSetTimeSpeed() {
        super(ServerOpcode.LoginSetTimeSpeed);
    }

    @Override
    public void write() {
        this.writePackedTime(serverTime);
        this.writePackedTime(gameTime);
        this.writeFloat(newSpeed);
        this.writeInt32(serverTimeHolidayOffset);
        this.writeInt32(gameTimeHolidayOffset);
    }
}
