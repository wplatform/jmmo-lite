package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.ServerPacket;

public class AuctionOwnerBidNotification extends ServerPacket {
    public auctionOwnerNotification info = new auctionOwnerNotification();
    public ObjectGuid bidder = ObjectGuid.EMPTY;
    public long minIncrement;

    public AuctionOwnerBidNotification() {
        super(ServerOpcode.AuctionOwnerBidNotification);
    }

    @Override
    public void write() {
        info.write(this);
        this.writeInt64(minIncrement);
        this.writeGuid(bidder);
    }
}
