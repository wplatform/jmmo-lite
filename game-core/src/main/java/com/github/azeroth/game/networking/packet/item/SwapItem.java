package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.WorldPacket;

public class SwapItem extends ClientPacket {
    public invUpdate inv = new invUpdate();

    public byte slotA;

    public byte containerSlotB;

    public byte slotB;

    public byte containerSlotA;

    public swapItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        inv = new invUpdate(this);
        containerSlotB = this.readUInt8();
        containerSlotA = this.readUInt8();
        slotB = this.readUInt8();
        slotA = this.readUInt8();
    }
}
