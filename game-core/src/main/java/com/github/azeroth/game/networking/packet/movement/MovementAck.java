package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.movement.model.MovementInfo;
import com.github.azeroth.game.networking.WorldPacket;

public final class MovementAck {
    public MovementInfo status;
    public int ackIndex;

    public void read(WorldPacket data) {
        status = MovementIOUtil.readMovementInfo(data);
        ackIndex = data.readInt32();
    }
}
