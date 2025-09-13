package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SortBankBags extends ClientPacket {
    public SortBankBags(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
