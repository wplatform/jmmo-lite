package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SortBags extends ClientPacket {
    public SortBags(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
