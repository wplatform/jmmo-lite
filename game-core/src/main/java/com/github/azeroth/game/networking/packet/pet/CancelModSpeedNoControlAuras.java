package com.github.azeroth.game.networking.packet.pet;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CancelModSpeedNoControlAuras extends ClientPacket {
    public ObjectGuid targetGUID = ObjectGuid.EMPTY;

    public CancelModSpeedNoControlAuras(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        targetGUID = this.readPackedGuid();
    }
}
