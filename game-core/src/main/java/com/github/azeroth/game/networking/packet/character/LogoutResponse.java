package com.github.azeroth.game.networking.packet.character;


public class LogoutResponse extends ServerPacket {
    public int logoutResult;
    public boolean instant = false;

    public LogoutResponse() {
        super(ServerOpcode.LogoutResponse);
    }

    @Override
    public void write() {
        this.writeInt32(logoutResult);
        this.writeBit(instant);
        this.flushBits();
    }
}
