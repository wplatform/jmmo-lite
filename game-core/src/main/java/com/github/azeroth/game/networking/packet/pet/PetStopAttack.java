package com.github.azeroth.game.networking.packet.pet;

import com.github.azeroth.game.networking.WorldPacket;

class PetStopAttack extends ClientPacket {
    public ObjectGuid petGUID = ObjectGuid.EMPTY;

    public PetStopAttack(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        petGUID = this.readPackedGuid();
    }
}
