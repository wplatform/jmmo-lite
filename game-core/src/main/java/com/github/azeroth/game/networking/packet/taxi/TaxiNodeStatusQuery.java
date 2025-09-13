package com.github.azeroth.game.networking.packet.taxi;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class TaxiNodeStatusQuery extends ClientPacket {
    public ObjectGuid unitGUID = ObjectGuid.EMPTY;

    public TaxiNodeStatusQuery(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        unitGUID = this.readPackedGuid();
    }
}
