package com.github.azeroth.game.networking.packet.trade;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class AcceptTrade extends ClientPacket {
    public int stateIndex;

    public AcceptTrade(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        stateIndex = this.readUInt32();
    }
}
