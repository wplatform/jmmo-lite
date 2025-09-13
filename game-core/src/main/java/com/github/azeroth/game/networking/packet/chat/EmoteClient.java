package com.github.azeroth.game.networking.packet.chat;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class EmoteClient extends ClientPacket {
    public EmoteClient(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
