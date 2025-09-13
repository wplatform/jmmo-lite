package com.github.azeroth.game.networking.packet.battlepet;

import com.github.azeroth.game.networking.WorldPacket;

class BattlePetUpdateNotify extends ClientPacket {
    public ObjectGuid petGuid = ObjectGuid.EMPTY;

    public BattlePetUpdateNotify(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        petGuid = this.readPackedGuid();
    }
}
