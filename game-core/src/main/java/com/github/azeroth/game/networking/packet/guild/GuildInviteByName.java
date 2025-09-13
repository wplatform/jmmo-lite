package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildInviteByName extends ClientPacket {
    public String name;
    public Integer unused910 = null;

    public GuildInviteByName(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        var nameLen = this.<Integer>readBit(9);
        var hasUnused910 = this.readBit();

        name = this.readString(nameLen);

        if (hasUnused910) {
            unused910 = this.readInt32();
        }
    }
}
