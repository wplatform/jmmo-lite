package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.WorldPacket;

import java.time.LocalDateTime;


public class TimeSyncResponse extends ClientPacket {
    public int clientTime; // Client ticks in ms
    public int sequenceIndex; // Same index as in request

    public TimeSyncResponse(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        sequenceIndex = this.readUInt();
        clientTime = this.readUInt();
    }

    public final LocalDateTime getReceivedTime() {
        return this.getReceivedTime();
    }
}
