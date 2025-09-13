package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ConvertRaid extends ClientPacket {
    public boolean raid;

    public ConvertRaid(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        raid = this.readBit();
    }
}
