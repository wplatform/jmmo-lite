package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;
import io.netty.buffer.ByteBuf;

public class MovementSpeedAck extends ClientPacket {
    public MovementAck ack = new MovementAck();
    public float speed;

    protected MovementSpeedAck(ByteBuf data) {
        super(data);
    }

    @Override
    public void read() {
        ack.read(this);
        speed = this.readFloat();
    }
}
