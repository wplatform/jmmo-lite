package com.github.azeroth.game.networking.packet.query;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class QueryTime extends ClientPacket {
    public QueryTime(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
