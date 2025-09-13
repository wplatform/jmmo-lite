package com.github.azeroth.game.networking.packet.vehicle;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class RideVehicleInteract extends ClientPacket {
    public ObjectGuid vehicle = ObjectGuid.EMPTY;

    public RideVehicleInteract(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        vehicle = this.readPackedGuid();
    }
}
