package com.github.azeroth.game.networking.packet.trade;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class InitiateTrade extends ClientPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;

    public InitiateTrade(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        guid = this.readPackedGuid();
    }
}
