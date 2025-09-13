package com.github.azeroth.game.networking.packet.lfg;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class DFTeleport extends ClientPacket {
    public boolean teleportOut;

    public DFTeleport(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        teleportOut = this.readBit();
    }
}
