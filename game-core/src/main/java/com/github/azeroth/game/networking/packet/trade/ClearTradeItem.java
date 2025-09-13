package com.github.azeroth.game.networking.packet.trade;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class ClearTradeItem extends ClientPacket {
    public byte tradeSlot;

    public ClearTradeItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        tradeSlot = this.readUInt8();
    }
}
