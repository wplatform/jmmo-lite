package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;
import io.netty.buffer.ByteBuf;

public class WorldPortResponse extends ClientPacket {

    public WorldPortResponse(ByteBuf data) {
        super(data);
    }

    @Override
    public void read() {
    }
}
