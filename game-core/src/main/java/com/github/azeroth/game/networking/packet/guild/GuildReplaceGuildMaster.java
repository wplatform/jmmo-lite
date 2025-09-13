package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class GuildReplaceGuildMaster extends ClientPacket {
    public GuildReplaceGuildMaster(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
