package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.WorldPacket;

public class ItemGemData {
    public byte slot;
    public itemInstance item = new itemInstance();

    public final void write(WorldPacket data) {
        data.writeInt8(slot);
        item.write(data);
    }

    public final void read(WorldPacket data) {
        slot = data.readUInt8();
        item.read(data);
    }
}
