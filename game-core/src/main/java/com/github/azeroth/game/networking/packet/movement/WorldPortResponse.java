package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class WorldPortResponse extends ClientPacket {
    public WorldPortResponse(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
