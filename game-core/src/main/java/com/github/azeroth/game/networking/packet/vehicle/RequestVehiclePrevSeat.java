package com.github.azeroth.game.networking.packet.vehicle;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class RequestVehiclePrevSeat extends ClientPacket {
    public RequestVehiclePrevSeat(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
