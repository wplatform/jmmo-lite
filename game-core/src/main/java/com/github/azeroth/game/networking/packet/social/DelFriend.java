package com.github.azeroth.game.networking.packet.social;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class DelFriend extends ClientPacket {
    public qualifiedGUID player = new qualifiedGUID();

    public DelFriend(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        player.read(this);
    }
}
