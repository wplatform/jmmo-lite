package com.github.azeroth.game.networking.packet.areatrigger;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


class AreaTriggerSplineInfo {
    public int timeToTarget;
    public int elapsedTimeForMovement;
    public ArrayList<Vector3> points = new ArrayList<>();

    public final void write(WorldPacket data) {
        data.writeInt32(timeToTarget);
        data.writeInt32(elapsedTimeForMovement);

        data.writeBits(points.size(), 16);
        data.flushBits();

        for (var point : points) {
            data.writeVector3(point);
        }
    }
}
