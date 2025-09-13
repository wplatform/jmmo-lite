package com.github.azeroth.game.networking.packet.loot;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


class LootItemPkt extends ClientPacket {
    public ArrayList<lootRequest> loot = new ArrayList<>();

    public LootItemPkt(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        var count = this.readUInt32();

        for (int i = 0; i < count; ++i) {
            var loot = new LootRequest();
            loot.object = this.readPackedGuid();
            loot.lootListID = this.readUInt8();

            loot.add(loot);
        }
    }
}
