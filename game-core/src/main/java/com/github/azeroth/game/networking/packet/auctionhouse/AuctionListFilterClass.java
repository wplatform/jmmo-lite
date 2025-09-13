package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.WorldPacket;

public class AuctionListFilterClass {
    public int itemClass;
    public Array<AuctionListFilterSubClass> subClassFilters = new Array<AuctionListFilterSubClass>(31);

    public AuctionListFilterClass(WorldPacket data) {
        itemClass = data.readInt32();
        var subClassFilterCount = data.<Integer>readBit(5);

        for (var i = 0; i < subClassFilterCount; ++i) {
            subClassFilters.set(i, new AuctionListFilterSubClass(data));
        }
    }
}
