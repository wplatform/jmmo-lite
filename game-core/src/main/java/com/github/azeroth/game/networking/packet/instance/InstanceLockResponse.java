package com.github.azeroth.game.networking.packet.instance;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class InstanceLockResponse extends ClientPacket {
    public boolean acceptLock;

    public InstanceLockResponse(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        acceptLock = this.readBit();
    }
}
