package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.WorldPacket;

public final class AuctionListFilterSubClass {
    public int itemSubclass;
    public long invTypeMask;

    public AuctionListFilterSubClass() {
    }

    public AuctionListFilterSubClass(WorldPacket data) {
        invTypeMask = data.readUInt64();
        itemSubclass = data.readInt32();
    }

    public AuctionListFilterSubClass clone() {
        AuctionListFilterSubClass varCopy = new AuctionListFilterSubClass();

        varCopy.itemSubclass = this.itemSubclass;
        varCopy.invTypeMask = this.invTypeMask;

        return varCopy;
    }
}
