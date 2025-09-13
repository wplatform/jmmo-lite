package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.WorldPacket;

public class MovementMonsterSpline {
    public int id;
    public Vector3 destination;
    public boolean crzTeleport;
    public byte stopDistanceTolerance; // Determines how far from spline destination the mover is allowed to stop in place 0, 0, 3.0, 2.76, numeric_limits<float>::max, 1.1, float(INT_MAX); default before this field existed was distance 3.0 (index 2)
    public movementSpline move;

    public MovementMonsterSpline() {
        move = new MovementSpline();
    }

    public final void write(WorldPacket data) {
        data.writeInt32(id);
        data.writeVector3(destination);
        data.writeBit(crzTeleport);
        data.writeBits(stopDistanceTolerance, 3);

        move.write(data);
    }
}
