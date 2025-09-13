package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CancelGrowthAura extends ClientPacket {
    public CancelGrowthAura(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
