package com.github.azeroth.game.networking.packet.character;


public class LogoutComplete extends ServerPacket {
    public LogoutComplete() {
        super(ServerOpcode.LogoutComplete);
    }

    @Override
    public void write() {
    }
}
