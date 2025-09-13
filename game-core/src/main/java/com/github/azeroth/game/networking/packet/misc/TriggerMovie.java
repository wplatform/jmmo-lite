package com.github.azeroth.game.networking.packet.misc;


public class TriggerMovie extends ServerPacket {
    public int movieID;

    public TriggerMovie() {
        super(ServerOpcode.TriggerMovie);
    }

    @Override
    public void write() {
        this.writeInt32(movieID);
    }
}
