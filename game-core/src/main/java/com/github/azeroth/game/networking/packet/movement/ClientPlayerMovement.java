package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.movement.model.MovementInfo;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class ClientPlayerMovement extends ClientPacket {
    public MovementInfo status;

    public ClientPlayerMovement(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        status = MovementIOUtil.readMovementInfo(this);
    }
}
