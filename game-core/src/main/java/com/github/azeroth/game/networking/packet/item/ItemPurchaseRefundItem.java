package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.WorldPacket;

final class ItemPurchaseRefundItem {
    public int itemID;
    public int itemCount;

    public void write(WorldPacket data) {
        data.writeInt32(itemID);
        data.writeInt32(itemCount);
    }

    public ItemPurchaseRefundItem clone() {
        ItemPurchaseRefundItem varCopy = new ItemPurchaseRefundItem();

        varCopy.itemID = this.itemID;
        varCopy.itemCount = this.itemCount;

        return varCopy;
    }
}
