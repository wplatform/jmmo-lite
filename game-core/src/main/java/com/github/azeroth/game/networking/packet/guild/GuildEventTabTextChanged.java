package com.github.azeroth.game.networking.packet.guild;


public class GuildEventTabTextChanged extends ServerPacket {
    public int tab;

    public GuildEventTabTextChanged() {
        super(ServerOpcode.GuildEventTabTextChanged);
    }

    @Override
    public void write() {
        this.writeInt32(tab);
    }
}
