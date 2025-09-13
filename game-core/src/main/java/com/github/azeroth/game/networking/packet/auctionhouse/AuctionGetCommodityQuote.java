package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class AuctionGetCommodityQuote extends ClientPacket {
    public ObjectGuid auctioneer = ObjectGuid.EMPTY;
    public int itemID;
    public int quantity;
    public AddOnInfo taintedBy = null;

    public AuctionGetCommodityQuote(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        auctioneer = this.readPackedGuid();
        itemID = this.readInt32();
        quantity = this.readUInt32();

        if (this.readBit()) {
            taintedBy = new AddOnInfo();
            taintedBy.getValue().read(this);
        }
    }
}
