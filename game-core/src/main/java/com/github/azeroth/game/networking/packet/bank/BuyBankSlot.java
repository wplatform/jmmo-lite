package com.github.azeroth.game.networking.packet.bank;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class BuyBankSlot extends ClientPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;

    public BuyBankSlot(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        guid = this.readPackedGuid();
    }
}
