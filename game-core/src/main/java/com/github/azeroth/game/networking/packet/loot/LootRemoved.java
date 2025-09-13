package com.github.azeroth.game.networking.packet.loot;

import com.github.azeroth.game.networking.ServerPacket;

public class LootRemoved extends ServerPacket {
    public ObjectGuid lootObj = ObjectGuid.EMPTY;
    public ObjectGuid owner = ObjectGuid.EMPTY;
    public byte lootListID;

    public LootRemoved() {
        super(ServerOpcode.LootRemoved);
    }

    @Override
    public void write() {
        this.writeGuid(owner);
        this.writeGuid(lootObj);
        this.writeInt8(lootListID);
    }
}
