package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CancelMountAura extends ClientPacket {
    public CancelMountAura(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
