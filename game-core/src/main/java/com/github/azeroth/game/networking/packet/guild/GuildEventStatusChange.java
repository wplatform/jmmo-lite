package com.github.azeroth.game.networking.packet.guild;


import com.github.azeroth.game.networking.ServerPacket;

public class GuildEventStatusChange extends ServerPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;
    public boolean AFK;
    public boolean DND;

    public GuildEventStatusChange() {
        super(ServerOpcode.GuildEventStatusChange);
    }

    @Override
    public void write() {
        this.writeGuid(guid);
        this.writeBit(AFK);
        this.writeBit(DND);
        this.flushBits();
    }
}
