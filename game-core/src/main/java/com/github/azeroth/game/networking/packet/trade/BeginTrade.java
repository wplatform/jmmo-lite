package com.github.azeroth.game.networking.packet.trade;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class BeginTrade extends ClientPacket {
    public BeginTrade(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
