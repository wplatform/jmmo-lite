package com.github.azeroth.game.networking.packet.misc;


public class TimeSyncRequest extends ServerPacket {
    public int sequenceIndex;

    public TimeSyncRequest() {
        super(ServerOpcode.TimeSyncRequest);
    }

    @Override
    public void write() {
        this.writeInt32(sequenceIndex);
    }
}
