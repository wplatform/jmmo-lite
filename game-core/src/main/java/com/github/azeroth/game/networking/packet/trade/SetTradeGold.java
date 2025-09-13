package com.github.azeroth.game.networking.packet.trade;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class SetTradeGold extends ClientPacket {
    public long coinage;

    public SetTradeGold(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        coinage = this.readUInt64();
    }
}
