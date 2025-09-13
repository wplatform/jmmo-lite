package com.github.azeroth.game.networking.packet.loot;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class LootMoney extends ClientPacket {
    public LootMoney(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
