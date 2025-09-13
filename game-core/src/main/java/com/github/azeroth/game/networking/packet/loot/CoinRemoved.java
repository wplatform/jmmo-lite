package com.github.azeroth.game.networking.packet.loot;

import com.github.azeroth.game.networking.ServerPacket;

class CoinRemoved extends ServerPacket {
    public ObjectGuid lootObj = ObjectGuid.EMPTY;

    public CoinRemoved() {
        super(ServerOpcode.CoinRemoved);
    }

    @Override
    public void write() {
        this.writeGuid(lootObj);
    }
}
