package com.github.azeroth.game.networking.packet.instance;

import com.github.azeroth.game.networking.ServerPacket;

public class ResetFailedNotify extends ServerPacket {
    public ResetFailedNotify() {
        super(ServerOpcode.ResetFailedNotify);
    }

    @Override
    public void write() {
    }
}
