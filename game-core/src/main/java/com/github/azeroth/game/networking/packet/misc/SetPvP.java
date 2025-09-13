package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SetPvP extends ClientPacket {
    public boolean enablePVP;

    public setPvP(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        enablePVP = this.readBit();
    }
}
