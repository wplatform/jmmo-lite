package com.github.azeroth.game.networking.packet.mythicplus;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class MythicPlusRun {
    public int mapChallengeModeID;
    public boolean completed;
    public int level;
    public int durationMs;
    public long startDate;
    public long completionDate;
    public int season;
    public ArrayList<MythicPlusMember> members = new ArrayList<>();
    public float runScore;
    public int[] keystoneAffixIDs = new int[4];

    public final void write(WorldPacket data) {
        data.writeInt32(mapChallengeModeID);
        data.writeInt32(level);
        data.writeInt32(durationMs);
        data.writeInt64(startDate);
        data.writeInt64(completionDate);
        data.writeInt32(season);

        for (var id : keystoneAffixIDs) {
            data.writeInt32(id);
        }

        data.writeInt32(members.size());
        data.writeFloat(runScore);

        for (var member : members) {
            member.write(data);
        }

        data.writeBit(completed);
        data.flushBits();
    }
}
