package com.github.azeroth.game.networking.packet.character;


public class PlayedTime extends ServerPacket {
    public int totalTime;
    public int levelTime;
    public boolean triggerEvent;

    public PlayedTime() {
        super(ServerOpcode.PlayedTime);
    }

    @Override
    public void write() {
        this.writeInt32(totalTime);
        this.writeInt32(levelTime);
        this.writeBit(triggerEvent);
        this.flushBits();
    }
}
