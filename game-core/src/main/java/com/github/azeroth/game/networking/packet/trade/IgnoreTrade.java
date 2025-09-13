package com.github.azeroth.game.networking.packet.trade;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class IgnoreTrade extends ClientPacket {
    public IgnoreTrade(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
