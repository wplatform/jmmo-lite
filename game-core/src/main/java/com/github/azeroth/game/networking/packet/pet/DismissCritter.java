package com.github.azeroth.game.networking.packet.pet;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class DismissCritter extends ClientPacket {
    public ObjectGuid critterGUID = ObjectGuid.EMPTY;

    public DismissCritter(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        critterGUID = this.readPackedGuid();
    }
}

//Structs

