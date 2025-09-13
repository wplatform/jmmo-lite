package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class AuctionHelloRequest extends ClientPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;

    public AuctionHelloRequest(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        guid = this.readPackedGuid();
    }
}
