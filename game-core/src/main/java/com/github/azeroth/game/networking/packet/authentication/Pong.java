package com.github.azeroth.game.networking.packet.authentication;

import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class Pong extends ServerPacket {
    private final int serial;

    public Pong(int serial) {
        super(ServerOpCode.SMSG_PONG);
        this.serial = serial;
    }

    @Override
    public void write() {
        this.writeInt32(serial);
    }
}
