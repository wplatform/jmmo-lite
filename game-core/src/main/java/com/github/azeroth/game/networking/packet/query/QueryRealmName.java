package com.github.azeroth.game.networking.packet.query;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class QueryRealmName extends ClientPacket {

    public int virtualRealmAddress;

    public QueryRealmName(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        virtualRealmAddress = this.readUInt32();
    }
}
