package com.github.azeroth.game.networking.packet.guild;


public class GuildEventTabAdded extends ServerPacket {
    public GuildEventTabAdded() {
        super(ServerOpcode.GuildEventTabAdded);
    }

    @Override
    public void write() {
    }
}
