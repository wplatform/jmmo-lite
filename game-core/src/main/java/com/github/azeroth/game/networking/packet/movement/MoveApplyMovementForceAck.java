package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.movement.movementForce;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class MoveApplyMovementForceAck extends ClientPacket {
    public Movementack ack = new movementAck();
    public Movementforce force = new movementForce();

    public MoveApplyMovementForceAck(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        ack.read(this);
        force.read(this);
    }
}
