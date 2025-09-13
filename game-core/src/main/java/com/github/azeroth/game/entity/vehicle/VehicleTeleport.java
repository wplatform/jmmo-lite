package com.github.azeroth.game.entity.vehicle;


//ORIGINAL LINE: public struct VehicleTeleport
public final class VehicleTeleport {
    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public byte VehicleSeatIndex;
    public byte vehicleSeatIndex;
    public boolean vehicleExitVoluntary;
    public boolean vehicleExitTeleport;

    public VehicleTeleport clone() {
        VehicleTeleport varCopy = new VehicleTeleport();

        varCopy.VehicleSeatIndex = this.VehicleSeatIndex;
        varCopy.VehicleExitVoluntary = this.VehicleExitVoluntary;
        varCopy.VehicleExitTeleport = this.VehicleExitTeleport;

        return varCopy;
    }
}