package com.github.azeroth.game.networking.packet.reputation;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class RequestForcedReactions extends ClientPacket {
    public RequestForcedReactions(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
