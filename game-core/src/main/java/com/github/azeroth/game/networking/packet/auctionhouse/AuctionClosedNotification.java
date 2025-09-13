package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.ServerPacket;

public class AuctionClosedNotification extends ServerPacket {
    public auctionOwnerNotification info = new auctionOwnerNotification();
    public float proceedsMailDelay;
    public boolean sold = true;

    public AuctionClosedNotification() {
        super(ServerOpcode.AuctionClosedNotification);
    }

    @Override
    public void write() {
        info.write(this);
        this.writeFloat(proceedsMailDelay);
        this.writeBit(sold);
        this.flushBits();
    }
}
