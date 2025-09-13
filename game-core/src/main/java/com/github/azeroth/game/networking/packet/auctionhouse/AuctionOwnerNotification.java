package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.WorldPacket;
import game.*;

public final class AuctionOwnerNotification {
    public int auctionID;
    public long bidAmount;
    public itemInstance item;

    public void initialize(AuctionPosting auction) {
        auctionID = auction.id;
        item = new itemInstance(auction.items.get(0));
        bidAmount = auction.bidAmount;
    }

    public void write(WorldPacket data) {
        data.writeInt32(auctionID);
        data.writeInt64(bidAmount);
        item.write(data);
    }

    public AuctionOwnerNotification clone() {
        AuctionOwnerNotification varCopy = new auctionOwnerNotification();

        varCopy.auctionID = this.auctionID;
        varCopy.bidAmount = this.bidAmount;
        varCopy.item = this.item;

        return varCopy;
    }
}
