package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class AuctionCancelCommoditiesPurchase extends ClientPacket {
    public ObjectGuid auctioneer = ObjectGuid.EMPTY;
    public AddOnInfo taintedBy = null;

    public AuctionCancelCommoditiesPurchase(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        auctioneer = this.readPackedGuid();

        if (this.readBit()) {
            taintedBy = new AddOnInfo();
            taintedBy.getValue().read(this);
        }
    }
}
