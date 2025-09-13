package com.github.azeroth.game.networking.packet.achievement;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class AllAchievements {
    public ArrayList<EarnedAchievement> earned = new ArrayList<>();
    public ArrayList<CriteriaProgressPkt> progress = new ArrayList<>();

    public final void write(WorldPacket data) {
        data.writeInt32(earned.size());
        data.writeInt32(progress.size());

        for (var earned : earned) {
            earned.write(data);
        }

        for (var progress : progress) {
            progress.write(data);
        }
    }
}
