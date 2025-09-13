package com.github.azeroth.game.networking.packet.bpay;

import com.github.azeroth.game.networking.WorldPacket;

public final class GetPurchaseListQuery extends ClientPacket {
    public GetPurchaseListQuery(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
