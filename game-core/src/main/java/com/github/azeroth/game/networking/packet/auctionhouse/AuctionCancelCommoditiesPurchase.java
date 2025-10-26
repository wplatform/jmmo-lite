package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.networking.packet.addon.AddOnInfo;
import io.netty.buffer.ByteBuf;

public class AuctionCancelCommoditiesPurchase extends ClientPacket {
    public ObjectGuid auctioneer;
    public AddOnInfo taintedBy;

    public AuctionCancelCommoditiesPurchase(ByteBuf data) {
        super(data);
    }


    @Override
    public void read() {
        auctioneer = this.readPackedGuid();

        if (this.readBit()) {
            taintedBy = new AddOnInfo();
            taintedBy.read(this);
        }
    }
}
