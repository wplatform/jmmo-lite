package com.github.azeroth.game.networking.packet.authentication;

import com.github.azeroth.game.networking.ServerPacket;

public class WaitQueueFinish extends ServerPacket {
    public WaitQueueFinish() {
        super(ServerOpcode.WaitQueueFinish);
    }

    @Override
    public void write() {
    }
}
