package com.github.azeroth.game.networking.packet.loot;

import com.github.azeroth.game.networking.ServerPacket;

class AELootTargetsAck extends ServerPacket {
    public AELootTargetsAck() {
        super(ServerOpcode.AeLootTargetAck);
    }

    @Override
    public void write() {
    }
}
