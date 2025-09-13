package com.github.azeroth.game.networking.packet.guild;


import java.util.ArrayList;


public class GuildRoster extends ServerPacket {
    public ArrayList<GuildRostermemberData> memberData;
    public String welcomeText;
    public String infoText;
    public int createDate;
    public int numAccounts;
    public int guildFlags;

    public GuildRoster() {
        super(ServerOpcode.GuildRoster);
        memberData = new ArrayList<>();
    }

    @Override
    public void write() {
        this.writeInt32(numAccounts);
        this.writePackedTime(createDate);
        this.writeInt32(guildFlags);
        this.writeInt32(memberData.size());
        this.writeBits(welcomeText.getBytes().length, 11);
        this.writeBits(infoText.getBytes().length, 10);
        this.flushBits();

        memberData.forEach(p -> p.write(this));

        this.writeString(welcomeText);
        this.writeString(infoText);
    }
}
