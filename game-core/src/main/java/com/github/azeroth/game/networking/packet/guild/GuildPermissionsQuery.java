package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildPermissionsQuery extends ClientPacket {
    public GuildPermissionsQuery(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
