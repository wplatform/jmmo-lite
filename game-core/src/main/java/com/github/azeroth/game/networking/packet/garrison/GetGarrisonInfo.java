package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class GetGarrisonInfo extends ClientPacket {
    public GetGarrisonInfo(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
