package com.github.azeroth.game.networking.packet.guild;


public class GuildPartyState extends ServerPacket {
    public float guildXPEarnedMult = 0.0f;
    public int numMembers;
    public int numRequired;
    public boolean inGuildParty;

    public GuildPartyState() {
        super(ServerOpcode.GuildPartyState);
    }

    @Override
    public void write() {
        this.writeBit(inGuildParty);
        this.flushBits();

        this.writeInt32(numMembers);
        this.writeInt32(numRequired);
        this.writeFloat(guildXPEarnedMult);
    }
}
