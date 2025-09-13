package com.github.azeroth.game.networking.packet.blackmarket;

import com.github.azeroth.game.networking.ServerPacket;

public class BlackMarketBidOnItemResult extends ServerPacket {
    public int marketID;
    public itemInstance item;
    public BlackMarketError result = BlackMarketError.values()[0];

    public BlackMarketBidOnItemResult() {
        super(ServerOpcode.BlackMarketBidOnItemResult);
    }

    @Override
    public void write() {
        this.writeInt32(marketID);
        this.writeInt32((int) result.getValue());
        item.write(this);
    }
}
