package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class UseCritterItem extends ClientPacket {
    public ObjectGuid itemGuid = ObjectGuid.EMPTY;

    public UseCritterItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        itemGuid = this.readPackedGuid();
    }
}
