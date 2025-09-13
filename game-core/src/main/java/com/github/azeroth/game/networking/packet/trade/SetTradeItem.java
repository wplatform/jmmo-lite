package com.github.azeroth.game.networking.packet.trade;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class SetTradeItem extends ClientPacket {
    public byte tradeSlot;
    public byte packSlot;
    public byte itemSlotInPack;

    public SetTradeItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        tradeSlot = this.readUInt8();
        packSlot = this.readUInt8();
        itemSlotInPack = this.readUInt8();
    }
}
