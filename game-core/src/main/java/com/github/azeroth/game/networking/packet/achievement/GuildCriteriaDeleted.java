package com.github.azeroth.game.networking.packet.achievement;


public class GuildCriteriaDeleted extends ServerPacket {
    public ObjectGuid guildGUID = ObjectGuid.EMPTY;
    public int criteriaID;

    public GuildCriteriaDeleted() {
        super(ServerOpcode.GuildCriteriaDeleted);
    }

    @Override
    public void write() {
        this.writeGuid(guildGUID);
        this.writeInt32(criteriaID);
    }
}
