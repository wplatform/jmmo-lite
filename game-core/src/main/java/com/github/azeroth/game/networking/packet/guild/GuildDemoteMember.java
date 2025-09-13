package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildDemoteMember extends ClientPacket {
    public ObjectGuid demotee = ObjectGuid.EMPTY;

    public GuildDemoteMember(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        demotee = this.readPackedGuid();
    }
}
