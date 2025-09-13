package com.github.azeroth.game.networking.packet.vehicle;


public class MoveSetVehicleRecID extends ServerPacket {
    public ObjectGuid moverGUID = ObjectGuid.EMPTY;
    public int sequenceIndex;
    public int vehicleRecID;

    public MoveSetVehicleRecID() {
        super(ServerOpcode.MoveSetVehicleRecId);
    }

    @Override
    public void write() {
        this.writeGuid(moverGUID);
        this.writeInt32(sequenceIndex);
        this.writeInt32(vehicleRecID);
    }
}
