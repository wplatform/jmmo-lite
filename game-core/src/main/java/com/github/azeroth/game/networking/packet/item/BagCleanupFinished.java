package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.ServerPacket;

public class BagCleanupFinished extends ServerPacket {
    public BagCleanupFinished() {
        super(ServerOpcode.BagCleanupFinished);
    }

    @Override
    public void write() {
    }
}
