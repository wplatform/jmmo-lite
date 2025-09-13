package com.github.azeroth.game.networking.packet.guild;


public class GuildInvite extends ServerPacket {
    public ObjectGuid guildGUID = ObjectGuid.EMPTY;
    public ObjectGuid oldGuildGUID = ObjectGuid.EMPTY;
    public int achievementPoints;
    public int emblemColor;
    public int emblemStyle;
    public int borderStyle;
    public int borderColor;
    public int background;
    public int guildVirtualRealmAddress;
    public int oldGuildVirtualRealmAddress;
    public int inviterVirtualRealmAddress;
    public String inviterName;
    public String guildName;
    public String oldGuildName;

    public GuildInvite() {
        super(ServerOpcode.GuildInvite);
    }

    @Override
    public void write() {
        this.writeBits(inviterName.getBytes().length, 6);
        this.writeBits(guildName.getBytes().length, 7);
        this.writeBits(oldGuildName.getBytes().length, 7);

        this.writeInt32(inviterVirtualRealmAddress);
        this.writeInt32(guildVirtualRealmAddress);
        this.writeGuid(guildGUID);
        this.writeInt32(oldGuildVirtualRealmAddress);
        this.writeGuid(oldGuildGUID);
        this.writeInt32(emblemStyle);
        this.writeInt32(emblemColor);
        this.writeInt32(borderStyle);
        this.writeInt32(borderColor);
        this.writeInt32(background);
        this.writeInt32(achievementPoints);

        this.writeString(inviterName);
        this.writeString(guildName);
        this.writeString(oldGuildName);
    }
}
