package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SetWarMode extends ClientPacket {
    public boolean enable;

    public SetWarMode(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        enable = this.readBit();
    }
}
