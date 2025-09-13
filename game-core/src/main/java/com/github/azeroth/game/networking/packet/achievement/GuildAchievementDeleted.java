package com.github.azeroth.game.networking.packet.achievement;


public class GuildAchievementDeleted extends ServerPacket {
    public ObjectGuid guildGUID = ObjectGuid.EMPTY;
    public int achievementID;
    public long timeDeleted;

    public GuildAchievementDeleted() {
        super(ServerOpcode.GuildAchievementDeleted);
    }

    @Override
    public void write() {
        this.writeGuid(guildGUID);
        this.writeInt32(achievementID);
        this.writePackedTime(timeDeleted);
    }
}
