package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class DestroyItem extends ClientPacket {
    public int count;
    public byte slotNum;
    public byte containerId;

    public destroyItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        count = this.readUInt32();
        containerId = this.readUInt8();
        slotNum = this.readUInt8();
    }
}
