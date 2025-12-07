package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class FlightSplineSync extends ServerPacket {
    public ObjectGuid guid;
    public float splineDist;

    public FlightSplineSync() {
        super(ServerOpCode.SMSG_FLIGHT_SPLINE_SYNC);
    }

    @Override
    public void write() {
        this.writeGuid(guid);
        this.writeFloat(splineDist);
    }
}
