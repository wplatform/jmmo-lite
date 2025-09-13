package com.github.azeroth.game.networking.packet.pet;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class PetAbandon extends ClientPacket {
    public ObjectGuid pet = ObjectGuid.EMPTY;

    public PetAbandon(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        pet = this.readPackedGuid();
    }
}
