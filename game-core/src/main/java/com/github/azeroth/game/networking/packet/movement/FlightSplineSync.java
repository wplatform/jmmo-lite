package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.ServerPacket;

public class FlightSplineSync extends ServerPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;
    public float splineDist;

    public FlightSplineSync() {
        super(ServerOpcode.FlightSplineSync);
    }

    @Override
    public void write() {
        this.writeGuid(guid);
        this.writeFloat(splineDist);
    }
}
