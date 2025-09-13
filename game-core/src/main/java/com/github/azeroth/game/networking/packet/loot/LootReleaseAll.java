package com.github.azeroth.game.networking.packet.loot;

import com.github.azeroth.game.networking.ServerPacket;

public class LootReleaseAll extends ServerPacket {
    public LootReleaseAll() {
        super(ServerOpcode.LootReleaseAll);
    }

    @Override
    public void write() {
    }
}
