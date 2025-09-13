package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildChangeNameRequest extends ClientPacket {
    public String newName;

    public GuildChangeNameRequest(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        var nameLen = this.<Integer>readBit(7);
        newName = this.readString(nameLen);
    }
}
