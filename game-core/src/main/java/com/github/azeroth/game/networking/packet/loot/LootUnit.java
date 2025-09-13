package com.github.azeroth.game.networking.packet.loot;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class LootUnit extends ClientPacket {
    public ObjectGuid unit = ObjectGuid.EMPTY;
    public boolean isSoftInteract;

    public LootUnit(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        unit = this.readPackedGuid();
        isSoftInteract = this.readBit();
    }
}

//Structs

