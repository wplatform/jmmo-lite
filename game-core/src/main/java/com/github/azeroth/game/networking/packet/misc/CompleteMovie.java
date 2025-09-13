package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CompleteMovie extends ClientPacket {
    public CompleteMovie(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
