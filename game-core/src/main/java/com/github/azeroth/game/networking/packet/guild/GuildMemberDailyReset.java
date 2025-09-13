package com.github.azeroth.game.networking.packet.guild;


public class GuildMemberDailyReset extends ServerPacket {
    public GuildMemberDailyReset() {
        super(ServerOpcode.GuildMemberDailyReset);
    }

    @Override
    public void write() {
    }
}
