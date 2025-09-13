package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.entity.ObjectGuid;
import com.github.azeroth.game.networking.WorldPacket;

public final class AuctionItemForSale {
    public ObjectGuid guid = ObjectGuid.EMPTY;

    public int useCount;

    public AuctionItemForSale() {
    }

    public AuctionItemForSale(WorldPacket data) {
        guid = data.readPackedGuid();
        useCount = data.readUInt32();
    }

    public AuctionItemForSale clone() {
        AuctionItemForSale varCopy = new AuctionItemForSale();

        varCopy.guid = this.guid;
        varCopy.useCount = this.useCount;

        return varCopy;
    }
}
