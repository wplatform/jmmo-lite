package com.github.azeroth.game.networking.packet.query;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class QueryCreature extends ClientPacket {
    public int creatureID;

    public QueryCreature(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        creatureID = this.readUInt32();
    }
}
