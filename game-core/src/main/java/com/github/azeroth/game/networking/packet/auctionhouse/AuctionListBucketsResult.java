package com.github.azeroth.game.networking.packet.auctionhouse;


import java.util.ArrayList;


public class AuctionListBucketsResult extends ServerPacket {
    public ArrayList<BucketInfo> buckets = new ArrayList<>();
    public int desiredDelay;
    public int unknown830_0;
    public int unknown830_1;
    public AuctionHousebrowseMode browseMode = AuctionHouseBrowseMode.values()[0];
    public boolean hasMoreResults;

    public AuctionListBucketsResult() {
        super(ServerOpcode.AuctionListBucketsResult);
    }

    @Override
    public void write() {
        this.writeInt32(buckets.size());
        this.writeInt32(desiredDelay);
        this.writeInt32(unknown830_0);
        this.writeInt32(unknown830_1);
        this.writeBits(browseMode.getValue(), 1);
        this.writeBit(hasMoreResults);
        this.flushBits();

        for (var bucketInfo : buckets) {
            bucketInfo.write(this);
        }
    }
}
