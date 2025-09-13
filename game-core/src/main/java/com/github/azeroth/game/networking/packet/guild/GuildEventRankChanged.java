package com.github.azeroth.game.networking.packet.guild;


public class GuildEventRankChanged extends ServerPacket {
    public int rankID;

    public GuildEventRankChanged() {
        super(ServerOpcode.GuildEventRankChanged);
    }

    @Override
    public void write() {
        this.writeInt32(rankID);
    }
}
