package com.github.azeroth.game.networking.packet.pet;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class PetSpellAutocast extends ClientPacket {
    public ObjectGuid petGUID = ObjectGuid.EMPTY;

    public int spellID;
    public boolean autocastEnabled;

    public PetSpellAutocast(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        petGUID = this.readPackedGuid();
        spellID = this.readUInt32();
        autocastEnabled = this.readBit();
    }
}
