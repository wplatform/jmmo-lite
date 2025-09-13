package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class FarSight extends ClientPacket {
    public boolean enable;

    public FarSight(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        enable = this.readBit();
    }
}
