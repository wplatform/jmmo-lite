package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class SortReagentBankBags extends ClientPacket {
    public SortReagentBankBags(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
