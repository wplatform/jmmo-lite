package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.WorldPacket;

public final class MissileTrajectoryRequest {
    public float pitch;
    public float speed;

    public void read(WorldPacket data) {
        pitch = data.readFloat();
        speed = data.readFloat();
    }

    public MissileTrajectoryRequest clone() {
        MissileTrajectoryRequest varCopy = new missileTrajectoryRequest();

        varCopy.pitch = this.pitch;
        varCopy.speed = this.speed;

        return varCopy;
    }
}
