package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class MoveRemoveMovementForceAck extends ClientPacket {
    public Movementack ack = new movementAck();
    public ObjectGuid ID = ObjectGuid.EMPTY;

    public MoveRemoveMovementForceAck(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        ack.read(this);
        ID = this.readPackedGuid();
    }
}
