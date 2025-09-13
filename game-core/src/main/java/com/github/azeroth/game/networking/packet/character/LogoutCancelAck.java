package com.github.azeroth.game.networking.packet.character;


public class LogoutCancelAck extends ServerPacket {
    public LogoutCancelAck() {
        super(ServerOpcode.LogoutCancelAck);
    }

    @Override
    public void write() {
    }
}
