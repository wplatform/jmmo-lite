package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class MovementSpeedAck extends ClientPacket {
    public Movementack ack = new movementAck();
    public float speed;

    public MovementSpeedAck(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        ack.read(this);
        speed = this.readFloat();
    }
}
