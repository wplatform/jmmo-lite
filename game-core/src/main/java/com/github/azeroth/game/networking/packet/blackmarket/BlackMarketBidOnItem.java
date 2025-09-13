package com.github.azeroth.game.networking.packet.blackmarket;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class BlackMarketBidOnItem extends ClientPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;
    public int marketID;
    public itemInstance item = new itemInstance();
    public long bidAmount;

    public BlackMarketBidOnItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        guid = this.readPackedGuid();
        marketID = this.readUInt32();
        bidAmount = this.readUInt64();
        item.read(this);
    }
}
