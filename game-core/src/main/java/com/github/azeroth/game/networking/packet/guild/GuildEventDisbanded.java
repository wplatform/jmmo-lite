package com.github.azeroth.game.networking.packet.guild;


public class GuildEventDisbanded extends ServerPacket {
    public GuildEventDisbanded() {
        super(ServerOpcode.GuildEventDisbanded);
    }

    @Override
    public void write() {
    }
}
