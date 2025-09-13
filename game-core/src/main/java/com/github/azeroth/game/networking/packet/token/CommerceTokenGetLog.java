package com.github.azeroth.game.networking.packet.token;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CommerceTokenGetLog extends ClientPacket {
    public int unkInt;

    public CommerceTokenGetLog(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        unkInt = this.readUInt32();
    }
}
