package com.github.azeroth.game.networking.packet.auctionhouse;


import com.github.azeroth.game.networking.WorldPacket;

public final class AuctionSortDef {
    public AuctionHousesortOrder sortOrder = AuctionHouseSortOrder.values()[0];
    public boolean reverseSort;

    public AuctionSortDef() {
    }

    public AuctionSortDef(AuctionHouseSortOrder sortOrder, boolean reverseSort) {
        sortOrder = sortOrder;
        reverseSort = reverseSort;
    }

    public AuctionSortDef(WorldPacket data) {
        data.resetBitPos();
        sortOrder = AuctionHouseSortOrder.forValue(data.readBit(4));
        reverseSort = data.readBit();
    }

    public AuctionSortDef clone() {
        AuctionSortDef varCopy = new AuctionSortDef();

        varCopy.sortOrder = this.sortOrder;
        varCopy.reverseSort = this.reverseSort;

        return varCopy;
    }
}
