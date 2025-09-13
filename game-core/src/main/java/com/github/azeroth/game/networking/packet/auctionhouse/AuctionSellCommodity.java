package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class AuctionSellCommodity extends ClientPacket {
    public ObjectGuid auctioneer = ObjectGuid.EMPTY;
    public long unitPrice;
    public int runTime;
    public AddOnInfo taintedBy = null;
    public Array<AuctionItemForSale> items = new Array<AuctionItemForSale>(64);

    public AuctionSellCommodity(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        auctioneer = this.readPackedGuid();
        unitPrice = this.readUInt64();
        runTime = this.readUInt32();

        if (this.readBit()) {
            taintedBy = new AddOnInfo();
        }

        var itemCount = this.<Integer>readBit(6);

        if (taintedBy != null) {
            taintedBy.getValue().read(this);
        }

        for (var i = 0; i < itemCount; ++i) {
            items.set(i, new AuctionItemForSale(this));
        }
    }
}
