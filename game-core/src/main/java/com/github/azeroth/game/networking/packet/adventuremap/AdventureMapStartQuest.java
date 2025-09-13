package com.github.azeroth.game.networking.packet.adventuremap;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class AdventureMapStartQuest extends ClientPacket {
    public int questID;

    public AdventureMapStartQuest(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        questID = this.readUInt32();
    }
}
