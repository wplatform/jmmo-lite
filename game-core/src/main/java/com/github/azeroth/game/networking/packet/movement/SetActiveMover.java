package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class SetActiveMover extends ClientPacket {
    public ObjectGuid activeMover = ObjectGuid.EMPTY;

    public SetActiveMover(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        activeMover = this.readPackedGuid();
    }
}
