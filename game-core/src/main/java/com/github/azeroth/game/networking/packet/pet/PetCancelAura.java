package com.github.azeroth.game.networking.packet.pet;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class PetCancelAura extends ClientPacket {
    public ObjectGuid petGUID = ObjectGuid.EMPTY;
    public int spellID;

    public PetCancelAura(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        petGUID = this.readPackedGuid();
        spellID = this.readUInt32();
    }
}
