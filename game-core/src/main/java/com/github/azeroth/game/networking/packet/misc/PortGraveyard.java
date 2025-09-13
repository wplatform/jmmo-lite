package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class PortGraveyard extends ClientPacket {
    public PortGraveyard(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
