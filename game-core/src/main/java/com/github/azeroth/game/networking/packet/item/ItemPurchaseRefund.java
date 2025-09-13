package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ItemPurchaseRefund extends ClientPacket {
    public ObjectGuid itemGUID = ObjectGuid.EMPTY;

    public ItemPurchaseRefund(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        itemGUID = this.readPackedGuid();
    }
}
