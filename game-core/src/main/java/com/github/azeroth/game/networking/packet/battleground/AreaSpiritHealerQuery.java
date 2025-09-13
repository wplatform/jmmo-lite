package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class AreaSpiritHealerQuery extends ClientPacket {
    public ObjectGuid healerGuid = ObjectGuid.EMPTY;

    public AreaSpiritHealerQuery(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        healerGuid = this.readPackedGuid();
    }
}
