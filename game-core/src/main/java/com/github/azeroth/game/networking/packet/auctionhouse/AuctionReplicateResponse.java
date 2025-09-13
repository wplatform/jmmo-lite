package com.github.azeroth.game.networking.packet.auctionhouse;


import java.util.ArrayList;


public class AuctionReplicateResponse extends ServerPacket {
    public int changeNumberCursor;
    public int changeNumberGlobal;
    public int desiredDelay;
    public int changeNumberTombstone;
    public int result;
    public ArrayList<AuctionItem> items = new ArrayList<>();

    public AuctionReplicateResponse() {
        super(ServerOpcode.AuctionReplicateResponse);
    }

    @Override
    public void write() {
        this.writeInt32(result);
        this.writeInt32(desiredDelay);
        this.writeInt32(changeNumberGlobal);
        this.writeInt32(changeNumberCursor);
        this.writeInt32(changeNumberTombstone);
        this.writeInt32(items.size());

        for (var item : items) {
            item.write(this);
        }
    }
}
