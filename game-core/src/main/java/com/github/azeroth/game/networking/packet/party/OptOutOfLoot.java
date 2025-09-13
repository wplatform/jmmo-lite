package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class OptOutOfLoot extends ClientPacket {
    public boolean passOnLoot;

    public OptOutOfLoot(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        passOnLoot = this.readBit();
    }
}
