package com.github.azeroth.game.networking.packet.duel;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class CanDuel extends ClientPacket {
    public ObjectGuid targetGUID = ObjectGuid.EMPTY;

    public CanDuel(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        targetGUID = this.readPackedGuid();
    }
}
