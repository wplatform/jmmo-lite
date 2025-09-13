package com.github.azeroth.game.networking.packet.vehicle;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class RequestVehicleExit extends ClientPacket {
    public RequestVehicleExit(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
