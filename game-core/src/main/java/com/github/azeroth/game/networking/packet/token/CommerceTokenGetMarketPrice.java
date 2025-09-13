package com.github.azeroth.game.networking.packet.token;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CommerceTokenGetMarketPrice extends ClientPacket {
    public int unkInt;

    public CommerceTokenGetMarketPrice(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        unkInt = this.readUInt32();
    }
}
