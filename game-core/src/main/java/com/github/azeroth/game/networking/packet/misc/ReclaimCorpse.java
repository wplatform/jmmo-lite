package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class ReclaimCorpse extends ClientPacket {
    public ObjectGuid corpseGUID = ObjectGuid.EMPTY;

    public ReclaimCorpse(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        corpseGUID = this.readPackedGuid();
    }
}
