package com.github.azeroth.game.networking.packet.npc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class RequestStabledPets extends ClientPacket {
    public ObjectGuid stableMaster = ObjectGuid.EMPTY;

    public RequestStabledPets(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        stableMaster = this.readPackedGuid();
    }
}
