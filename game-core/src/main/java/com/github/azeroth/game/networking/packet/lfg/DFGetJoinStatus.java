package com.github.azeroth.game.networking.packet.lfg;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class DFGetJoinStatus extends ClientPacket {
    public DFGetJoinStatus(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
