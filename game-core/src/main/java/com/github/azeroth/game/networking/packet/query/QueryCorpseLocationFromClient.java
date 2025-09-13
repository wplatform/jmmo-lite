package com.github.azeroth.game.networking.packet.query;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class QueryCorpseLocationFromClient extends ClientPacket {
    public ObjectGuid player = ObjectGuid.EMPTY;

    public QueryCorpseLocationFromClient(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        player = this.readPackedGuid();
    }
}
