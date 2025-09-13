package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildSetGuildMaster extends ClientPacket {
    public String newMasterName;

    public GuildSetGuildMaster(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        var nameLen = this.<Integer>readBit(9);
        newMasterName = this.readString(nameLen);
    }
}
