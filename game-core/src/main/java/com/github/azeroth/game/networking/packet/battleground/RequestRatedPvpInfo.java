package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class RequestRatedPvpInfo extends ClientPacket {
    public RequestRatedPvpInfo(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
