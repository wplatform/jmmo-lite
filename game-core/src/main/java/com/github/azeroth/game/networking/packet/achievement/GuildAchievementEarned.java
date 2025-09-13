package com.github.azeroth.game.networking.packet.achievement;


public class GuildAchievementEarned extends ServerPacket {
    public int achievementID;
    public ObjectGuid guildGUID = ObjectGuid.EMPTY;
    public long timeEarned;

    public GuildAchievementEarned() {
        super(ServerOpcode.GuildAchievementEarned);
    }

    @Override
    public void write() {
        this.writeGuid(guildGUID);
        this.writeInt32(achievementID);
        this.writePackedTime(timeEarned);
    }
}
