package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class GetPVPOptionsEnabled extends ClientPacket {
    public GetPVPOptionsEnabled(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
