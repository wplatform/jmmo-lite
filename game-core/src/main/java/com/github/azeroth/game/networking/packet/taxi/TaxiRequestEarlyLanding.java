package com.github.azeroth.game.networking.packet.taxi;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class TaxiRequestEarlyLanding extends ClientPacket {
    public TaxiRequestEarlyLanding(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
