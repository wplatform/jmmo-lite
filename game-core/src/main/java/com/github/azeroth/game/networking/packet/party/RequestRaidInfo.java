package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class RequestRaidInfo extends ClientPacket {
    public RequestRaidInfo(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
