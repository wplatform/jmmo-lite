package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class KeyboundOverride extends ClientPacket {

    public short overrideID;

    public KeyboundOverride(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        overrideID = this.readUInt16();
    }
}
