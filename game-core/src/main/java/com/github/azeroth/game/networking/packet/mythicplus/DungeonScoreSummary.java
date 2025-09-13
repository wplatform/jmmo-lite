package com.github.azeroth.game.networking.packet.mythicplus;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;

public class DungeonScoreSummary {
    public float overallScoreCurrentSeason;
    public float ladderScoreCurrentSeason;
    public ArrayList<DungeonScoreMapSummary> runs = new ArrayList<>();

    public final void write(WorldPacket data) {
        data.writeFloat(overallScoreCurrentSeason);
        data.writeFloat(ladderScoreCurrentSeason);
        data.writeInt32(runs.size());

        for (var dungeonScoreMapSummary : runs) {
            dungeonScoreMapSummary.write(data);
        }
    }
}
