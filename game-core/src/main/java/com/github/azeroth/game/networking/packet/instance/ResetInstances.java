package com.github.azeroth.game.networking.packet.instance;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ResetInstances extends ClientPacket {
    public resetInstances(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
