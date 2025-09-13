package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.WorldPacket;

class ItemPurchaseContents {
    public long money;
    public ItemPurchaseRefundItem[] items = new ItemPurchaseRefundItem[5];
    public ItemPurchaseRefundCurrency[] currencies = new ItemPurchaseRefundCurrency[5];

    public final void write(WorldPacket data) {
        data.writeInt64(money);

        for (var i = 0; i < 5; ++i) {
            Items[i].write(data);
        }

        for (var i = 0; i < 5; ++i) {
            Currencies[i].write(data);
        }
    }
}
