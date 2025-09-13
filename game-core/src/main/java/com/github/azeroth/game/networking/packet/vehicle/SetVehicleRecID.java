package com.github.azeroth.game.networking.packet.vehicle;


public class SetVehicleRecID extends ServerPacket {
    public ObjectGuid vehicleGUID = ObjectGuid.EMPTY;
    public int vehicleRecID;

    public SetVehicleRecID() {
        super(ServerOpcode.SetVehicleRecId);
    }

    @Override
    public void write() {
        this.writeGuid(vehicleGUID);
        this.writeInt32(vehicleRecID);
    }
}
