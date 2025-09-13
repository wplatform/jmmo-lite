package com.github.azeroth.game.networking.packet.bpay;

import com.github.azeroth.game.networking.WorldPacket;

public final class GetProductList extends ClientPacket {
    public GetProductList(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
