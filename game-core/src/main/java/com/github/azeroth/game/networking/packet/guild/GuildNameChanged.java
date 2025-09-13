package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ServerPacket;

public class GuildNameChanged extends ServerPacket {
    public ObjectGuid guildGUID = ObjectGuid.EMPTY;
    public String guildName;

    public GuildNameChanged() {
        super(ServerOpcode.GuildNameChanged);
    }

    @Override
    public void write() {
        this.writeGuid(guildGUID);
        this.writeBits(guildName.getBytes().length, 7);
        this.flushBits();
        this.writeString(guildName);
    }
}
