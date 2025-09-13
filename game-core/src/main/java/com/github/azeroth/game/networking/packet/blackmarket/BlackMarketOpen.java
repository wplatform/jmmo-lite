package com.github.azeroth.game.networking.packet.blackmarket;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class BlackMarketOpen extends ClientPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;

    public BlackMarketOpen(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        guid = this.readPackedGuid();
    }
}
