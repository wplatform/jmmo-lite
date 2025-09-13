package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class SwapInvItem extends ClientPacket {
    public invUpdate inv = new invUpdate();
    public byte slot1; // Source Slot
    public byte slot2; // Destination Slot

    public SwapInvItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        inv = new invUpdate(this);
        slot2 = this.readUInt8();
        slot1 = this.readUInt8();
    }
}
