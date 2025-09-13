package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class RequestPVPRewards extends ClientPacket {
    public RequestPVPRewards(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
