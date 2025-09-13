package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class SplitItem extends ClientPacket {
    public byte toSlot;
    public byte toPackSlot;
    public byte fromPackSlot;
    public int quantity;
    public invUpdate inv = new invUpdate();
    public byte fromSlot;

    public splitItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        inv = new invUpdate(this);
        fromPackSlot = this.readUInt8();
        fromSlot = this.readUInt8();
        toPackSlot = this.readUInt8();
        toSlot = this.readUInt8();
        quantity = this.readInt32();
    }
}
