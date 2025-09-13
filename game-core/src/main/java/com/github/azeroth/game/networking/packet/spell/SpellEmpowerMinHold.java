package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SpellEmpowerMinHold extends ClientPacket {
    public float holdPct;

    public SpellEmpowerMinHold(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        holdPct = this.readFloat();
    }
}
