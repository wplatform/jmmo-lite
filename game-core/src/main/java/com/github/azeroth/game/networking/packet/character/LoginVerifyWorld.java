package com.github.azeroth.game.networking.packet.character;


public class LoginVerifyWorld extends ServerPacket {
    public int mapID = -1;
    public position pos;
    public int reason = 0;

    public LoginVerifyWorld() {
        super(ServerOpcode.LoginVerifyWorld);
    }

    @Override
    public void write() {
        this.writeInt32(mapID);
        this.writeFloat(pos.getX());
        this.writeFloat(pos.getY());
        this.writeFloat(pos.getZ());
        this.writeFloat(pos.getO());
        this.writeInt32(reason);
    }
}
