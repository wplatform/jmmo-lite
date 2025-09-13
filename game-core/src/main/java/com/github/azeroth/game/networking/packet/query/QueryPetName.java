package com.github.azeroth.game.networking.packet.query;

import com.github.azeroth.game.networking.WorldPacket;

class QueryPetName extends ClientPacket {
    public ObjectGuid unitGUID = ObjectGuid.EMPTY;

    public QueryPetName(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        unitGUID = this.readPackedGuid();
    }
}
