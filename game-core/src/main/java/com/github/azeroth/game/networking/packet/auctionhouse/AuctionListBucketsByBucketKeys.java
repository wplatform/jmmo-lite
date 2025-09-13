package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.WorldPacket;

class AuctionListBucketsByBucketKeys extends ClientPacket {
    public ObjectGuid auctioneer = ObjectGuid.EMPTY;
    public AddOnInfo taintedBy = null;
    public Array<auctionBucketKey> bucketKeys = new Array<auctionBucketKey>(100);
    public Array<AuctionSortDef> sorts = new Array<AuctionSortDef>(2);

    public AuctionListBucketsByBucketKeys(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        auctioneer = this.readPackedGuid();

        if (this.readBit()) {
            taintedBy = new AddOnInfo();
        }

        var bucketKeysCount = this.<Integer>readBit(7);
        var sortCount = this.<Integer>readBit(2);

        for (var i = 0; i < sortCount; ++i) {
            sorts.set(i, new AuctionSortDef(this));
        }

        if (taintedBy != null) {
            taintedBy.getValue().read(this);
        }

        for (var i = 0; i < bucketKeysCount; ++i) {
            bucketKeys.set(i, new auctionBucketKey(this));
        }
    }
}
