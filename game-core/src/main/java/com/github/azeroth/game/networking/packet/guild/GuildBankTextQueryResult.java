package com.github.azeroth.game.networking.packet.guild;


public class GuildBankTextQueryResult extends ServerPacket {
    public int tab;
    public String text;

    public GuildBankTextQueryResult() {
        super(ServerOpcode.GuildBankTextQueryResult);
    }

    @Override
    public void write() {
        this.writeInt32(tab);

        this.writeBits(text.getBytes().length, 14);
        this.writeString(text);
    }
}
