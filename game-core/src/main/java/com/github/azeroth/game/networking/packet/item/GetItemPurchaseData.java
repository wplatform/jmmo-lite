package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GetItemPurchaseData extends ClientPacket {
    public ObjectGuid itemGUID = ObjectGuid.EMPTY;

    public GetItemPurchaseData(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        itemGUID = this.readPackedGuid();
    }
}
