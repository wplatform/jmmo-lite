package com.github.azeroth.game.networking.packet.scenario;

import com.github.azeroth.game.networking.WorldPacket;

class ScenarioSpellUpdate {
    public int spellID;
    public boolean usable = true;

    public final void write(WorldPacket data) {
        data.writeInt32(spellID);
        data.writeBit(usable);
        data.flushBits();
    }
}
