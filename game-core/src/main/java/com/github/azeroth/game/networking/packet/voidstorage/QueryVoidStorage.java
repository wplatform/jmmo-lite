package com.github.azeroth.game.networking.packet.voidstorage;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class QueryVoidStorage extends ClientPacket {
    public ObjectGuid npc = ObjectGuid.EMPTY;

    public QueryVoidStorage(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        npc = this.readPackedGuid();
    }
}
