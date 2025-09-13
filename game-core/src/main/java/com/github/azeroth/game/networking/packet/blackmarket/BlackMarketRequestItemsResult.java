package com.github.azeroth.game.networking.packet.blackmarket;


import java.util.ArrayList;


public class BlackMarketRequestItemsResult extends ServerPacket {
    public long lastUpdateID;
    public ArrayList<BlackMarketItem> items = new ArrayList<>();

    public BlackMarketRequestItemsResult() {
        super(ServerOpcode.BlackMarketRequestItemsResult);
    }

    @Override
    public void write() {
        this.writeInt64(lastUpdateID);
        this.writeInt32(items.size());

        for (var item : items) {
            item.write(this);
        }
    }
}
