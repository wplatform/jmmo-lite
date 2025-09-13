package com.github.azeroth.game.networking.packet.mythicplus;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class DungeonScoreData {
    public ArrayList<DungeonScoreSeasonData> seasons = new ArrayList<>();
    public int totalRuns;

    public final void write(WorldPacket data) {
        data.writeInt32(seasons.size());
        data.writeInt32(totalRuns);

        for (var season : seasons) {
            season.write(data);
        }
    }
}
