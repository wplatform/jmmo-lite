package com.github.azeroth.game.networking.packet.vehicle;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class RequestVehicleNextSeat extends ClientPacket {
    public RequestVehicleNextSeat(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
