package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class PVPLogDataRequest extends ClientPacket {
    public PVPLogDataRequest(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
