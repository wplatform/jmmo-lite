package com.github.azeroth.game.networking.packet.misc;


public class StartTimer extends ServerPacket {
    public int totalTime;
    public int timeLeft;
    public Timertype type = TimerType.values()[0];

    public startTimer() {
        super(ServerOpcode.startTimer);
    }

    @Override
    public void write() {
        this.writeInt32(totalTime);
        this.writeInt32(timeLeft);
        this.writeInt32(type.getValue());
    }
}
