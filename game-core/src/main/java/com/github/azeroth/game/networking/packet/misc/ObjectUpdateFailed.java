package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ObjectUpdateFailed extends ClientPacket {
    public ObjectGuid objectGUID = ObjectGuid.EMPTY;

    public ObjectUpdateFailed(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        objectGUID = this.readPackedGuid();
    }
}
