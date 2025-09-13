package com.github.azeroth.game.networking.packet.trait;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ClassTalentsDeleteConfig extends ClientPacket {
    public int configID;

    public ClassTalentsDeleteConfig(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        configID = this.readInt32();
    }
}
