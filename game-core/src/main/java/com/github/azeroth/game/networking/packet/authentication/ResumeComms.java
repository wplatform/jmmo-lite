package com.github.azeroth.game.networking.packet.authentication;

import com.github.azeroth.game.networking.ServerPacket;

public class ResumeComms extends ServerPacket {
    public ResumeComms(ConnectionType connection) {
        super(ServerOpcode.ResumeComms, connection);
    }

    @Override
    public void write() {
    }
}
