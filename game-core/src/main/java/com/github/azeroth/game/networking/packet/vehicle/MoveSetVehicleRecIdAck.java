package com.github.azeroth.game.networking.packet.vehicle;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class MoveSetVehicleRecIdAck extends ClientPacket {
    public movementAck data = new movementAck();
    public int vehicleRecID;

    public MoveSetVehicleRecIdAck(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        data.read(this);
        vehicleRecID = this.readInt32();
    }
}
