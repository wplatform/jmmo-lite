package com.github.azeroth.game.networking.packet.character;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SetWatchedFaction extends ClientPacket {
    public int factionIndex;

    public SetWatchedFaction(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        factionIndex = this.readUInt32();
    }
}
