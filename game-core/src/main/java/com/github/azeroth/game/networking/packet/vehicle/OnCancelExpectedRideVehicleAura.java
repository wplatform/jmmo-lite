package com.github.azeroth.game.networking.packet.vehicle;


public class OnCancelExpectedRideVehicleAura extends ServerPacket {
    public OnCancelExpectedRideVehicleAura() {
        super(ServerOpcode.OnCancelExpectedRideVehicleAura);
    }

    @Override
    public void write() {
    }
}
