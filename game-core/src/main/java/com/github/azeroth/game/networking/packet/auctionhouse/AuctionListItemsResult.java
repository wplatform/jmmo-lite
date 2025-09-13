package com.github.azeroth.game.networking.packet.auctionhouse;


import java.util.ArrayList;


public class AuctionListItemsResult extends ServerPacket {
    public ArrayList<AuctionItem> items = new ArrayList<>();
    public int unknown830;
    public int totalCount;
    public int desiredDelay;
    public AuctionHouselistType listType = AuctionHouseListType.values()[0];
    public boolean hasMoreResults;
    public AuctionbucketKey bucketKey = new auctionBucketKey();

    public AuctionListItemsResult() {
        super(ServerOpcode.AuctionListItemsResult);
    }

    @Override
    public void write() {
        this.writeInt32(items.size());
        this.writeInt32(unknown830);
        this.writeInt32(totalCount);
        this.writeInt32(desiredDelay);
        this.writeBits(listType.getValue(), 2);
        this.writeBit(hasMoreResults);
        this.flushBits();

        bucketKey.write(this);

        for (var item : items) {
            item.write(this);
        }
    }
}
