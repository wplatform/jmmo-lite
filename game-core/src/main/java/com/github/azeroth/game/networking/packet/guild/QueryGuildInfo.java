package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class QueryGuildInfo extends ClientPacket {
    public ObjectGuid guildGuid = ObjectGuid.EMPTY;
    public ObjectGuid playerGuid = ObjectGuid.EMPTY;

    public QueryGuildInfo(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        guildGuid = this.readPackedGuid();
        playerGuid = this.readPackedGuid();
    }
}

//Structs

