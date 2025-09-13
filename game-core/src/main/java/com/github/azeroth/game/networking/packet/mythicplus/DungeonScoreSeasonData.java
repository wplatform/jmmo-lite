package com.github.azeroth.game.networking.packet.mythicplus;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;

public class DungeonScoreSeasonData {
    public int season;
    public ArrayList<DungeonScoreMapData> seasonMaps = new ArrayList<>();
    public ArrayList<DungeonScoreMapData> ladderMaps = new ArrayList<>();
    public float seasonScore;
    public float ladderScore = 0.0f;

    public final void write(WorldPacket data) {
        data.writeInt32(season);
        data.writeInt32(seasonMaps.size());
        data.writeInt32(ladderMaps.size());
        data.writeFloat(seasonScore);
        data.writeFloat(ladderScore);

        for (var map : seasonMaps) {
            map.write(data);
        }

        for (var map : ladderMaps) {
            map.write(data);
        }
    }
}
