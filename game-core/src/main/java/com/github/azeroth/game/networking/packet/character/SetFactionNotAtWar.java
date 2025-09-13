package com.github.azeroth.game.networking.packet.character;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SetFactionNotAtWar extends ClientPacket {
    public byte factionIndex;

    public SetFactionNotAtWar(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        factionIndex = this.readUInt8();
    }
}
