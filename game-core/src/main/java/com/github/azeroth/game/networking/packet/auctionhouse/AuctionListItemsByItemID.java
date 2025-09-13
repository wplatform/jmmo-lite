package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class AuctionListItemsByItemID extends ClientPacket {
    public ObjectGuid auctioneer = ObjectGuid.EMPTY;
    public int itemID;
    public int suffixItemNameDescriptionID;
    public int offset;
    public AddOnInfo taintedBy = null;
    public Array<AuctionSortDef> sorts = new Array<AuctionSortDef>(2);

    public AuctionListItemsByItemID(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        auctioneer = this.readPackedGuid();
        itemID = this.readUInt32();
        suffixItemNameDescriptionID = this.readInt32();
        offset = this.readUInt32();

        if (this.readBit()) {
            taintedBy = new AddOnInfo();
        }

        var sortCount = this.<Integer>readBit(2);

        for (var i = 0; i < sortCount; ++i) {
            sorts.set(i, new AuctionSortDef(this));
        }

        if (taintedBy != null) {
            taintedBy.getValue().read(this);
        }
    }
}
