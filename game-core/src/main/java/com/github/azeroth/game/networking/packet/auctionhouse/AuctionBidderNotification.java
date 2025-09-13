package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.WorldPacket;
import game.*;

final class AuctionBidderNotification {
    public int auctionID;
    public ObjectGuid bidder = ObjectGuid.EMPTY;
    public itemInstance item;

    public void initialize(AuctionPosting auction, com.github.azeroth.game.entity.item.Item item) {
        auctionID = auction.id;
        item = new itemInstance(item);
        bidder = auction.bidder;
    }

    public void write(WorldPacket data) {
        data.writeInt32(auctionID);
        data.writeGuid(bidder);
        item.write(data);
    }

    public AuctionBidderNotification clone() {
        AuctionBidderNotification varCopy = new auctionBidderNotification();

        varCopy.auctionID = this.auctionID;
        varCopy.bidder = this.bidder;
        varCopy.item = this.item;

        return varCopy;
    }
}
