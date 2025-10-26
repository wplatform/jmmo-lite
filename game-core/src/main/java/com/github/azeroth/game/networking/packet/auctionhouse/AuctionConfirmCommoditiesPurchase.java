package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.networking.packet.addon.AddOnInfo;
import io.netty.buffer.ByteBuf;

public class AuctionConfirmCommoditiesPurchase extends ClientPacket {
    public ObjectGuid auctioneer;
    public int itemID;
    public int quantity;
    public AddOnInfo taintedBy = null;

    public AuctionConfirmCommoditiesPurchase(ByteBuf data) {
        super(data);
    }


    @Override
    public void read() {
        auctioneer = this.readPackedGuid();
        itemID = this.readInt32();
        quantity = this.readUInt32();

        if (this.readBit()) {
            taintedBy = new AddOnInfo();
            taintedBy.read(this);
        }
    }
}
