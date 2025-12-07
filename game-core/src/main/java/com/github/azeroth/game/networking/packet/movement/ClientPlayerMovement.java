package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.movement.model.MovementInfo;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;
import io.netty.buffer.ByteBuf;

public class ClientPlayerMovement extends ClientPacket {
    public MovementInfo status;

    protected ClientPlayerMovement(ByteBuf data) {
        super(data);
    }


    @Override
    public void read() {
        status = MovementIOUtil.readMovementInfo(this);
    }
}
