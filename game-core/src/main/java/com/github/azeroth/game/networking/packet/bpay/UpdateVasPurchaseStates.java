package com.github.azeroth.game.networking.packet.bpay;

import com.github.azeroth.game.networking.WorldPacket;

public final class UpdateVasPurchaseStates extends ClientPacket {
    public UpdateVasPurchaseStates(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
