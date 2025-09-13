package com.github.azeroth.game.networking.packet.achievement;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildSetFocusedAchievement extends ClientPacket {

    public int achievementID;

    public GuildSetFocusedAchievement(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        achievementID = this.readUInt32();
    }
}
