package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.WorldPacket;

public final class MoveKnockBackSpeeds {
    public float horzSpeed;
    public float vertSpeed;

    public void write(WorldPacket data) {
        data.writeFloat(horzSpeed);
        data.writeFloat(vertSpeed);
    }

    public void read(WorldPacket data) {
        horzSpeed = data.readFloat();
        vertSpeed = data.readFloat();
    }

    public MoveKnockBackSpeeds clone() {
        MoveKnockBackSpeeds varCopy = new moveKnockBackSpeeds();

        varCopy.horzSpeed = this.horzSpeed;
        varCopy.vertSpeed = this.vertSpeed;

        return varCopy;
    }
}
