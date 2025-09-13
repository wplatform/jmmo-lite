package com.github.azeroth.game.networking.packet.authentication;

import com.github.azeroth.game.networking.ServerPacket;

public class Pong extends ServerPacket {
    private final int serial;

    public Pong(int serial) {
        super(ServerOpcode.Pong);
        serial = serial;
    }

    @Override
    public void write() {
        this.writeInt32(serial);
    }
}
