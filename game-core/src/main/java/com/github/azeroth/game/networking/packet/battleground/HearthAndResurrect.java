package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class HearthAndResurrect extends ClientPacket {
    public HearthAndResurrect(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
