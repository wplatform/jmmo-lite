package com.github.azeroth.game.networking.packet.query;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class QueryCorpseTransport extends ClientPacket {
    public ObjectGuid player = ObjectGuid.EMPTY;
    public ObjectGuid transport = ObjectGuid.EMPTY;

    public QueryCorpseTransport(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        player = this.readPackedGuid();
        transport = this.readPackedGuid();
    }
}
