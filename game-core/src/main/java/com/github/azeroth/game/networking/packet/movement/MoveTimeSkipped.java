package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class MoveTimeSkipped extends ClientPacket {
    public ObjectGuid moverGUID = ObjectGuid.EMPTY;
    public int timeSkipped;

    public MoveTimeSkipped(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        moverGUID = this.readPackedGuid();
        timeSkipped = this.readUInt32();
    }
}
