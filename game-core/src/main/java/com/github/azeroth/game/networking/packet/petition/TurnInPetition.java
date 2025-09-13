package com.github.azeroth.game.networking.packet.petition;

import com.github.azeroth.game.networking.WorldPacket;

public class TurnInPetition extends ClientPacket {
    public ObjectGuid item = ObjectGuid.EMPTY;

    public TurnInPetition(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        item = this.readPackedGuid();
    }
}
