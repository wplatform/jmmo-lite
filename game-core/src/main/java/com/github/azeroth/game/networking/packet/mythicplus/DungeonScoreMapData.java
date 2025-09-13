package com.github.azeroth.game.networking.packet.mythicplus;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class DungeonScoreMapData {
    public int mapChallengeModeID;
    public ArrayList<DungeonScoreBestRunForAffix> bestRuns = new ArrayList<>();
    public float overAllScore;

    public final void write(WorldPacket data) {
        data.writeInt32(mapChallengeModeID);
        data.writeInt32(bestRuns.size());
        data.writeFloat(overAllScore);

        for (var bestRun : bestRuns) {
            bestRun.write(data);
        }
    }
}
