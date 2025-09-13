package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CompleteCinematic extends ClientPacket {
    public CompleteCinematic(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
