package com.github.azeroth.game.networking.packet.battlepet;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class BattlePetSummon extends ClientPacket {
    public ObjectGuid petGuid = ObjectGuid.EMPTY;

    public BattlePetSummon(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        petGuid = this.readPackedGuid();
    }
}
