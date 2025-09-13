package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CloseInteraction extends ClientPacket {
    public ObjectGuid sourceGuid = ObjectGuid.EMPTY;

    public CloseInteraction(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        sourceGuid = this.readPackedGuid();
    }
}
