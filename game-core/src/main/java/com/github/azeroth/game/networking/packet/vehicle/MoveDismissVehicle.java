package com.github.azeroth.game.networking.packet.vehicle;

import com.github.azeroth.game.movement.model.MovementInfo;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class MoveDismissVehicle extends ClientPacket {
    public MovementInfo status;

    public MoveDismissVehicle(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        status = MovementExtensions.readMovementInfo(this);
    }
}
