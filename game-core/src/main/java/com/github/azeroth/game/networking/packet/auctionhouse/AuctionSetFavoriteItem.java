package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class AuctionSetFavoriteItem extends ClientPacket {
    public auctionFavoriteInfo item = new auctionFavoriteInfo();
    public boolean isNotFavorite = true;

    public AuctionSetFavoriteItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        isNotFavorite = this.readBit();
        item = new auctionFavoriteInfo(this);
    }
}
