package com.github.azeroth.game.networking.packet.blackmarket;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class BlackMarketRequestItems extends ClientPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;
    public long lastUpdateID;

    public BlackMarketRequestItems(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        guid = this.readPackedGuid();
        lastUpdateID = this.readInt64();
    }
}
