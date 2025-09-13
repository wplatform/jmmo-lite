package com.github.azeroth.game.networking.packet.petition;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class DeclinePetition extends ClientPacket {
    public ObjectGuid petitionGUID = ObjectGuid.EMPTY;

    public DeclinePetition(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        petitionGUID = this.readPackedGuid();
    }
}
