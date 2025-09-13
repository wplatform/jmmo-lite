package com.github.azeroth.game.networking.packet.character;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class LogoutRequest extends ClientPacket {
    public boolean idleLogout;

    public LogoutRequest(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        idleLogout = this.readBit();
    }
}
