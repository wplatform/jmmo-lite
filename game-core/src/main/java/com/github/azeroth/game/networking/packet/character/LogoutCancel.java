package com.github.azeroth.game.networking.packet.character;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class LogoutCancel extends ClientPacket {
    public LogoutCancel(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
