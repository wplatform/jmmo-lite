package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.ServerPacket;

public class AuctionGetCommodityQuoteResult extends ServerPacket {
    public Long totalPrice = null;
    public Integer quantity = null;
    public Integer quoteDuration = null;
    public int itemID;
    public int desiredDelay;

    public AuctionGetCommodityQuoteResult() {
        super(ServerOpcode.AuctionGetCommodityQuoteResult);
    }

    @Override
    public void write() {
        this.writeBit(totalPrice != null);
        this.writeBit(quantity != null);
        this.writeBit(quoteDuration != null);
        this.writeInt32(itemID);
        this.writeInt32(desiredDelay);

        if (totalPrice != null) {
            this.writeInt64(totalPrice.longValue());
        }

        if (quantity != null) {
            this.writeInt32(quantity.intValue());
        }

        if (quoteDuration != null) {
            this.writeInt32(quoteDuration.intValue());
        }
    }
}
