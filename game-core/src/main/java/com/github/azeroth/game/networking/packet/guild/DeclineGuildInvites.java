package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class DeclineGuildInvites extends ClientPacket {
    public boolean allow;

    public DeclineGuildInvites(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        allow = this.readBit();
    }
}
