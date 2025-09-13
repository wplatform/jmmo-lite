package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;

class AuctionListBiddedItems extends ClientPacket {
    public ObjectGuid auctioneer = ObjectGuid.EMPTY;

    public int offset;

    public ArrayList<Integer> auctionItemIDs = new ArrayList<>();
    public Array<AuctionSortDef> sorts = new Array<AuctionSortDef>(2);
    public AddOnInfo taintedBy = null;

    public AuctionListBiddedItems(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        auctioneer = this.readPackedGuid();
        offset = this.readUInt32();

        if (this.readBit()) {
            taintedBy = new AddOnInfo();
        }

        var auctionIDCount = this.<Integer>readBit(7);
        var sortCount = this.<Integer>readBit(2);

        for (var i = 0; i < sortCount; ++i) {
            sorts.set(i, new AuctionSortDef(this));
        }

        if (taintedBy != null) {
            taintedBy.getValue().read(this);
        }

        for (var i = 0; i < auctionIDCount; ++i) {
            auctionItemIDs.set(i, this.readUInt32());
        }
    }
}
