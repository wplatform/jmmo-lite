package com.github.azeroth.game.networking.packet.achievement;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class GuildGetAchievementMembers extends ClientPacket {
    public ObjectGuid playerGUID = ObjectGuid.EMPTY;
    public ObjectGuid guildGUID = ObjectGuid.EMPTY;
    public int achievementID;

    public GuildGetAchievementMembers(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        playerGUID = this.readPackedGuid();
        guildGUID = this.readPackedGuid();
        achievementID = this.readUInt32();
    }
}
