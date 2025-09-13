package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class AuctionListItemsByBucketKey extends ClientPacket {
    public ObjectGuid auctioneer = ObjectGuid.EMPTY;
    public int offset;
    public byte unknown830;
    public AddOnInfo taintedBy = null;
    public Array<AuctionSortDef> sorts = new Array<AuctionSortDef>(2);
    public AuctionbucketKey bucketKey;

    public AuctionListItemsByBucketKey(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        auctioneer = this.readPackedGuid();
        offset = this.readUInt32();
        unknown830 = this.readByte();

        if (this.readBit()) {
            taintedBy = new AddOnInfo();
        }

        var sortCount = this.<Integer>readBit(2);

        for (var i = 0; i < sortCount; ++i) {
            sorts.set(i, new AuctionSortDef(this));
        }

        bucketKey = new auctionBucketKey(this);

        if (taintedBy != null) {
            taintedBy.getValue().read(this);
        }
    }
}
