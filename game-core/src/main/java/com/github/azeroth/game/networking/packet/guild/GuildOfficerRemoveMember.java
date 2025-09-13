package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildOfficerRemoveMember extends ClientPacket {
    public ObjectGuid removee = ObjectGuid.EMPTY;

    public GuildOfficerRemoveMember(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        removee = this.readPackedGuid();
    }
}
