package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


class GuildSetAchievementTracking extends ClientPacket {
    public ArrayList<Integer> achievementIDs = new ArrayList<>();

    public GuildSetAchievementTracking(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        var count = this.readUInt32();

        for (int i = 0; i < count; ++i) {
            achievementIDs.add(this.readUInt32());
        }
    }
}
