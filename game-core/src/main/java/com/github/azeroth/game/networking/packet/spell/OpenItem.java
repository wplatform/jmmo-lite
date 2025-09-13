package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.WorldPacket;

public class OpenItem extends ClientPacket {
    public byte slot;
    public byte packSlot;

    public OpenItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        slot = this.readUInt8();
        packSlot = this.readUInt8();
    }
}
