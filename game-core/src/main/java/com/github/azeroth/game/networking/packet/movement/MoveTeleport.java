package com.github.azeroth.game.networking.packet.movement;


public class MoveTeleport extends ServerPacket {
    public position pos;
    public vehicleTeleport vehicle = null;
    public int sequenceIndex;
    public ObjectGuid moverGUID = ObjectGuid.EMPTY;
    public ObjectGuid transportGUID = null;
    public float facing;
    public byte preloadWorld;

    public MoveTeleport() {
        super(ServerOpcode.MoveTeleport);
    }

    @Override
    public void write() {
        this.writeGuid(moverGUID);
        this.writeInt32(sequenceIndex);
        this.writeXYZ(pos);
        this.writeFloat(facing);
        this.writeInt8(preloadWorld);

        this.writeBit(transportGUID != null);
        this.writeBit(vehicle != null);
        this.flushBits();

        if (vehicle != null) {
            this.writeInt8(vehicle.getValue().vehicleSeatIndex);
            this.writeBit(vehicle.getValue().vehicleExitVoluntary);
            this.writeBit(vehicle.getValue().vehicleExitTeleport);
            this.flushBits();
        }

        if (transportGUID != null) {
            this.writeGuid(transportGUID.getValue());
        }
    }
}
