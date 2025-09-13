package com.github.azeroth.game.networking.packet.toy;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ToyClearFanfare extends ClientPacket {
    public int itemID;

    public toyClearFanfare(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        itemID = this.readUInt32();
    }
}
