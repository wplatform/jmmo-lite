package com.github.azeroth.game.networking.packet.loot;

import com.github.azeroth.game.networking.ServerPacket;

public class LootAllPassed extends ServerPacket {
    public ObjectGuid lootObj = ObjectGuid.EMPTY;
    public LootitemData item = new lootItemData();

    public LootAllPassed() {
        super(ServerOpcode.LootAllPassed);
    }

    @Override
    public void write() {
        this.writeGuid(lootObj);
        item.write(this);
    }
}
