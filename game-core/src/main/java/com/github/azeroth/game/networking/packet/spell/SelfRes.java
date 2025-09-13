package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SelfRes extends ClientPacket {
    public int spellId;

    public SelfRes(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        spellId = this.readUInt32();
    }
}
