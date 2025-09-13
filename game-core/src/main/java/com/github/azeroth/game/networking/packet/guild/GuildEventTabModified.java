package com.github.azeroth.game.networking.packet.guild;


public class GuildEventTabModified extends ServerPacket {
    public String icon;
    public String name;
    public int tab;

    public GuildEventTabModified() {
        super(ServerOpcode.GuildEventTabModified);
    }

    @Override
    public void write() {
        this.writeInt32(tab);

        this.writeBits(name.getBytes().length, 7);
        this.writeBits(icon.getBytes().length, 9);
        this.flushBits();

        this.writeString(name);
        this.writeString(icon);
    }
}
