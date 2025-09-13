package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class MoveInitActiveMoverComplete extends ClientPacket {
    public int ticks;

    public MoveInitActiveMoverComplete(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        ticks = this.readUInt32();
    }
}
