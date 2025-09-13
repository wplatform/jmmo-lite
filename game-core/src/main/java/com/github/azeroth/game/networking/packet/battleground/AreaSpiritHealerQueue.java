package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class AreaSpiritHealerQueue extends ClientPacket {
    public ObjectGuid healerGuid = ObjectGuid.EMPTY;

    public AreaSpiritHealerQueue(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        healerGuid = this.readPackedGuid();
    }
}
