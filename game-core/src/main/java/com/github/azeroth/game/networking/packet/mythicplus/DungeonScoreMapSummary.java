package com.github.azeroth.game.networking.packet.mythicplus;

import com.github.azeroth.game.networking.WorldPacket;

public final class DungeonScoreMapSummary {
    public int challengeModeID;
    public float mapScore;
    public int bestRunLevel;
    public int bestRunDurationMS;
    public boolean finishedSuccess;

    public void write(WorldPacket data) {
        data.writeInt32(challengeModeID);
        data.writeFloat(mapScore);
        data.writeInt32(bestRunLevel);
        data.writeInt32(bestRunDurationMS);
        data.writeBit(finishedSuccess);
        data.flushBits();
    }

    public DungeonScoreMapSummary clone() {
        DungeonScoreMapSummary varCopy = new DungeonScoreMapSummary();

        varCopy.challengeModeID = this.challengeModeID;
        varCopy.mapScore = this.mapScore;
        varCopy.bestRunLevel = this.bestRunLevel;
        varCopy.bestRunDurationMS = this.bestRunDurationMS;
        varCopy.finishedSuccess = this.finishedSuccess;

        return varCopy;
    }
}
