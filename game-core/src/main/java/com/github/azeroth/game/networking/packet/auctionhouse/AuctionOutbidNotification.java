package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.ServerPacket;

public class AuctionOutbidNotification extends ServerPacket {
    public auctionBidderNotification info = new auctionBidderNotification();
    public long bidAmount;
    public long minIncrement;

    public AuctionOutbidNotification() {
        super(ServerOpcode.AuctionOutbidNotification);
    }

    @Override
    public void write() {
        info.write(this);
        this.writeInt64(bidAmount);
        this.writeInt64(minIncrement);
    }
}
