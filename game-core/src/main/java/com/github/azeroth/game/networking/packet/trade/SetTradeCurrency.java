package com.github.azeroth.game.networking.packet.trade;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class SetTradeCurrency extends ClientPacket {
    public int type;
    public int quantity;

    public SetTradeCurrency(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        type = this.readUInt32();
        quantity = this.readUInt32();
    }
}
