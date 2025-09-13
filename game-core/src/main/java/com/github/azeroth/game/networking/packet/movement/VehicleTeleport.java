package com.github.azeroth.game.networking.packet.movement;

public final class VehicleTeleport {
    public byte vehicleSeatIndex;
    public boolean vehicleExitVoluntary;
    public boolean vehicleExitTeleport;

    public VehicleTeleport clone() {
        VehicleTeleport varCopy = new VehicleTeleport();

        varCopy.vehicleSeatIndex = this.vehicleSeatIndex;
        varCopy.vehicleExitVoluntary = this.vehicleExitVoluntary;
        varCopy.vehicleExitTeleport = this.vehicleExitTeleport;

        return varCopy;
    }
}
