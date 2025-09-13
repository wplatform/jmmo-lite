package com.github.azeroth.game.networking.packet.loot;

import com.github.azeroth.game.networking.ServerPacket;

public class LootReleaseResponse extends ServerPacket {
    public ObjectGuid lootObj = ObjectGuid.EMPTY;
    public ObjectGuid owner = ObjectGuid.EMPTY;

    public LootReleaseResponse() {
        super(ServerOpcode.LootRelease);
    }

    @Override
    public void write() {
        this.writeGuid(lootObj);
        this.writeGuid(owner);
    }
}
