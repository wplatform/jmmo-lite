package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildEventLogQuery extends ClientPacket {
    public GuildEventLogQuery(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
