package com.github.azeroth.game.networking.packet.social;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class DelIgnore extends ClientPacket {
    public qualifiedGUID player = new qualifiedGUID();

    public DelIgnore(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        player.read(this);
    }
}
