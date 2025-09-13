package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.ServerPacket;

public class ItemExpirePurchaseRefund extends ServerPacket {
    public ObjectGuid itemGUID = ObjectGuid.EMPTY;

    public ItemExpirePurchaseRefund() {
        super(ServerOpcode.ItemExpirePurchaseRefund);
    }

    @Override
    public void write() {
        this.writeGuid(itemGUID);
    }
}
