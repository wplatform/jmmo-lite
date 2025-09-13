package com.github.azeroth.game.networking.packet.auctionhouse;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class AuctionFavoriteList extends ServerPacket {
    public int desiredDelay;
    public ArrayList<AuctionFavoriteInfo> items = new ArrayList<>();

    public AuctionFavoriteList() {
        super(ServerOpcode.AuctionFavoriteList);
    }

    @Override
    public void write() {
        this.writeInt32(desiredDelay);
        this.writeBits(items.size(), 7);
        this.flushBits();

        for (var favoriteInfo : items) {
            favoriteInfo.write(this);
        }
    }
}
