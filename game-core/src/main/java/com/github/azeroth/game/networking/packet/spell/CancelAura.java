package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CancelAura extends ClientPacket {
    public ObjectGuid casterGUID = ObjectGuid.EMPTY;
    public int spellID;

    public CancelAura(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        spellID = this.readUInt32();
        casterGUID = this.readPackedGuid();
    }
}

//Structs

