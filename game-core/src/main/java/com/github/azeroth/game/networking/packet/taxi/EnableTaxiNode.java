package com.github.azeroth.game.networking.packet.taxi;

import com.github.azeroth.game.entity.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class EnableTaxiNode extends ClientPacket {
    public ObjectGuid unit = ObjectGuid.EMPTY;

    public EnableTaxiNode(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        unit = this.readPackedGuid();
    }
}
