package com.github.azeroth.game.networking.packet.who;


public class WhoIsResponse extends ServerPacket {
    public String accountName;

    public WhoIsResponse() {
        super(ServerOpcode.WhoIs);
    }

    @Override
    public void write() {
        this.writeBits(accountName.getBytes().length, 11);
        this.writeString(accountName);
    }
}
