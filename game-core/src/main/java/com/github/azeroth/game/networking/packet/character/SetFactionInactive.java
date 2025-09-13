package com.github.azeroth.game.networking.packet.character;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SetFactionInactive extends ClientPacket {
    public int index;
    public boolean state;

    public SetFactionInactive(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        index = this.readUInt32();
        state = this.readBit();
    }
}
