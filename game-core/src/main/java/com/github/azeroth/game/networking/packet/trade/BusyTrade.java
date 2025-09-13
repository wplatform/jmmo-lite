package com.github.azeroth.game.networking.packet.trade;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class BusyTrade extends ClientPacket {
    public BusyTrade(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
