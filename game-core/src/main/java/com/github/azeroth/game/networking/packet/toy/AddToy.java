package com.github.azeroth.game.networking.packet.toy;

import com.github.azeroth.game.networking.WorldPacket;

class AddToy extends ClientPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;

    public addToy(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        guid = this.readPackedGuid();
    }
}
