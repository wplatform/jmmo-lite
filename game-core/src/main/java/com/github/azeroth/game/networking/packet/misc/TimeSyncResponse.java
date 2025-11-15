package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;
import io.netty.buffer.ByteBuf;

import java.time.LocalDateTime;


public class TimeSyncResponse extends ClientPacket {
    public int clientTime; // Client ticks in ms
    public int sequenceIndex; // Same index as in request

    public TimeSyncResponse(ByteBuf data) {
        super(data);
    }


    @Override
    public void read() {
        sequenceIndex = this.readInt32();
        clientTime = this.readInt32();
    }
}
