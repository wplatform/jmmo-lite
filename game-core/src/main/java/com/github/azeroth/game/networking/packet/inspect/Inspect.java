package com.github.azeroth.game.networking.packet.inspect;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class Inspect extends ClientPacket {
    public ObjectGuid target = ObjectGuid.EMPTY;

    public Inspect(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        target = this.readPackedGuid();
    }
}
