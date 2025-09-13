package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.WorldPacket;

public final class MonsterSplineAnimTierTransition {
    public int tierTransitionID;
    public int startTime;
    public int endTime;
    public byte animTier;

    public void write(WorldPacket data) {
        data.writeInt32(tierTransitionID);
        data.writeInt32(startTime);
        data.writeInt32(endTime);
        data.writeInt8(animTier);
    }

    public MonsterSplineAnimTierTransition clone() {
        MonsterSplineAnimTierTransition varCopy = new MonsterSplineAnimTierTransition();

        varCopy.tierTransitionID = this.tierTransitionID;
        varCopy.startTime = this.startTime;
        varCopy.endTime = this.endTime;
        varCopy.animTier = this.animTier;

        return varCopy;
    }
}
