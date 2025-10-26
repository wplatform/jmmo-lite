package com.github.azeroth.game.networking.packet.blackmarket;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.networking.packet.item.ItemInstance;
import io.netty.buffer.ByteBuf;

public class BlackMarketBidOnItem extends ClientPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;
    public int marketID;
    public ItemInstance item = new ItemInstance();
    public long bidAmount;

    protected BlackMarketBidOnItem(ByteBuf data) {
        super(data);
    }


    @Override
    public void read() {
        guid = this.readPackedGuid();
        marketID = this.readUInt32();
        bidAmount = this.readUInt64();
        item.read(this);
    }
}
