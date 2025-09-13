package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ReadItem extends ClientPacket {
    public byte packSlot;
    public byte slot;

    public ReadItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        packSlot = this.readUInt8();
        slot = this.readUInt8();
    }
}
