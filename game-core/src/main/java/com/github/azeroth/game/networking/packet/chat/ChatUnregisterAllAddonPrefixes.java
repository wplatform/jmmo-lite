package com.github.azeroth.game.networking.packet.chat;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ChatUnregisterAllAddonPrefixes extends ClientPacket {
    public ChatUnregisterAllAddonPrefixes(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
