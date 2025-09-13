package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildQueryNews extends ClientPacket {
    public ObjectGuid guildGUID = ObjectGuid.EMPTY;

    public GuildQueryNews(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        guildGUID = this.readPackedGuid();
    }
}
