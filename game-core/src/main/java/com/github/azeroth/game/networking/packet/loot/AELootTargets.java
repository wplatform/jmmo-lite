package com.github.azeroth.game.networking.packet.loot;

import com.github.azeroth.game.networking.ServerPacket;

public class AELootTargets extends ServerPacket {
    private final int count;

    public AELootTargets(int count) {
        super(ServerOpcode.AeLootTargets);
        count = count;
    }

    @Override
    public void write() {
        this.writeInt32(count);
    }
}
