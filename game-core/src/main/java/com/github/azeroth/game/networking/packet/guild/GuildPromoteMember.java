package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildPromoteMember extends ClientPacket {
    public ObjectGuid promotee = ObjectGuid.EMPTY;

    public GuildPromoteMember(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        promotee = this.readPackedGuid();
    }
}
