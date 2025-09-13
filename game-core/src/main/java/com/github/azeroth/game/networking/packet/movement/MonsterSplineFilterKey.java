package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.WorldPacket;

public final class MonsterSplineFilterKey {
    public short idx;

    public short speed;

    public void write(WorldPacket data) {
        data.writeInt16(idx);
        data.writeInt16(speed);
    }

    public MonsterSplineFilterKey clone() {
        MonsterSplineFilterKey varCopy = new MonsterSplineFilterKey();

        varCopy.idx = this.idx;
        varCopy.speed = this.speed;

        return varCopy;
    }
}
