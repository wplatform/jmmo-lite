package com.github.azeroth.game.networking.packet.auctionhouse;


import java.util.ArrayList;


public class AuctionListBiddedItemsResult extends ServerPacket {
    public ArrayList<AuctionItem> items = new ArrayList<>();
    public int desiredDelay;
    public boolean hasMoreResults;

    public AuctionListBiddedItemsResult() {
        super(ServerOpcode.AuctionListBiddedItemsResult);
    }

    @Override
    public void write() {
        this.writeInt32(items.size());
        this.writeInt32(desiredDelay);
        this.writeBit(hasMoreResults);
        this.flushBits();

        for (var item : items) {
            item.write(this);
        }
    }
}
