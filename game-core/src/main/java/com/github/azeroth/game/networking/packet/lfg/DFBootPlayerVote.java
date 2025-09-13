package com.github.azeroth.game.networking.packet.lfg;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class DFBootPlayerVote extends ClientPacket {
    public boolean vote;

    public DFBootPlayerVote(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        vote = this.readBit();
    }
}
