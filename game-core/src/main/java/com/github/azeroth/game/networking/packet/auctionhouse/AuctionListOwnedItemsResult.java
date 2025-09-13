package com.github.azeroth.game.networking.packet.auctionhouse;


import java.util.ArrayList;


public class AuctionListOwnedItemsResult extends ServerPacket {
    public ArrayList<AuctionItem> items = new ArrayList<>();
    public ArrayList<AuctionItem> soldItems = new ArrayList<>();
    public int desiredDelay;
    public boolean hasMoreResults;

    public AuctionListOwnedItemsResult() {
        super(ServerOpcode.AuctionListOwnedItemsResult);
    }

    @Override
    public void write() {
        this.writeInt32(items.size());
        this.writeInt32(soldItems.size());
        this.writeInt32(desiredDelay);
        this.writeBit(hasMoreResults);
        this.flushBits();

        for (var item : items) {
            item.write(this);
        }

        for (var item : soldItems) {
            item.write(this);
        }
    }
}
