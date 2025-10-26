package com.github.azeroth.game.networking.packet.trade;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;
import io.netty.buffer.ByteBuf;

public class AcceptTrade extends ClientPacket {
    public int stateIndex;

    public AcceptTrade(ByteBuf buf) {
        super(buf);
    }

    @Override
    public void read() {
        stateIndex = this.readUInt32();
    }
}
