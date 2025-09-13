package com.github.azeroth.game.networking.packet.battlepet;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class BattlePetClearFanfare extends ClientPacket {
    public ObjectGuid petGuid = ObjectGuid.EMPTY;

    public BattlePetClearFanfare(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        petGuid = this.readPackedGuid();
    }
}
