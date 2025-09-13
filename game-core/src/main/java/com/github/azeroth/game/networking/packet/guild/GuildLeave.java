package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildLeave extends ClientPacket {
    public GuildLeave(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
