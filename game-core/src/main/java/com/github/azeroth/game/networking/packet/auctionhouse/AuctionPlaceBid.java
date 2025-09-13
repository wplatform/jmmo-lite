package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class AuctionPlaceBid extends ClientPacket {
    public ObjectGuid auctioneer = ObjectGuid.EMPTY;
    public long bidAmount;
    public int auctionID;
    public AddOnInfo taintedBy = null;

    public AuctionPlaceBid(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        auctioneer = this.readPackedGuid();
        auctionID = this.readUInt32();
        bidAmount = this.readUInt64();

        if (this.readBit()) {
            taintedBy = new AddOnInfo();
            taintedBy.getValue().read(this);
        }
    }
}
