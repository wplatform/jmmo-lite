package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SpellEmpowerRelease extends ClientPacket {
    public int spellID;

    public SpellEmpowerRelease(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        spellID = this.readUInt32();
    }
}
