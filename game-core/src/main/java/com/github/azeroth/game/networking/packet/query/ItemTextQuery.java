package com.github.azeroth.game.networking.packet.query;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ItemTextQuery extends ClientPacket {
    public ObjectGuid id = ObjectGuid.EMPTY;

    public ItemTextQuery(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        id = this.readPackedGuid();
    }
}
