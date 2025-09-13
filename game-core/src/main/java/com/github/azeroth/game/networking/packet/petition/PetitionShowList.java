package com.github.azeroth.game.networking.packet.petition;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class PetitionShowList extends ClientPacket {
    public ObjectGuid petitionUnit = ObjectGuid.EMPTY;

    public PetitionShowList(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        petitionUnit = this.readPackedGuid();
    }
}
