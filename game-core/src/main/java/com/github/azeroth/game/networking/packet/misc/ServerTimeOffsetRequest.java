package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class ServerTimeOffsetRequest extends ClientPacket {
    public ServerTimeOffsetRequest(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
