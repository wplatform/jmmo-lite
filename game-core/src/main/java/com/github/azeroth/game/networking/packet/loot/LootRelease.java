package com.github.azeroth.game.networking.packet.loot;

import com.github.azeroth.game.entity.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class LootRelease extends ClientPacket {
    public ObjectGuid unit = ObjectGuid.EMPTY;

    public LootRelease(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        unit = this.readPackedGuid();
    }
}
