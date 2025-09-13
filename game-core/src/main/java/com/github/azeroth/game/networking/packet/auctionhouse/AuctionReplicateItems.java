package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.WorldPacket;

class AuctionReplicateItems extends ClientPacket {
    public ObjectGuid auctioneer = ObjectGuid.EMPTY;
    public int changeNumberGlobal;
    public int changeNumberCursor;
    public int changeNumberTombstone;
    public int count;
    public AddOnInfo taintedBy = null;

    public AuctionReplicateItems(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        auctioneer = this.readPackedGuid();
        changeNumberGlobal = this.readUInt();
        changeNumberCursor = this.readUInt();
        changeNumberTombstone = this.readUInt();
        count = this.readUInt();

        if (this.readBit()) {
            taintedBy = new AddOnInfo();
            taintedBy.getValue().read(this);
        }
    }
}
