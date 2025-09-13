package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.WorldPacket;

public final class MonsterSplineJumpExtraData {
    public float jumpGravity;
    public int startTime;
    public int duration;

    public void write(WorldPacket data) {
        data.writeFloat(jumpGravity);
        data.writeInt32(startTime);
        data.writeInt32(duration);
    }

    public MonsterSplineJumpExtraData clone() {
        MonsterSplineJumpExtraData varCopy = new MonsterSplineJumpExtraData();

        varCopy.jumpGravity = this.jumpGravity;
        varCopy.startTime = this.startTime;
        varCopy.duration = this.duration;

        return varCopy;
    }
}
