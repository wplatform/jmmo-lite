package com.github.azeroth.game.networking.packet.guild;


public class GuildSendRankChange extends ServerPacket {
    public ObjectGuid other = ObjectGuid.EMPTY;
    public ObjectGuid officer = ObjectGuid.EMPTY;
    public boolean promote;
    public int rankID;

    public GuildSendRankChange() {
        super(ServerOpcode.GuildSendRankChange);
    }

    @Override
    public void write() {
        this.writeGuid(officer);
        this.writeGuid(other);
        this.writeInt32(rankID);

        this.writeBit(promote);
        this.flushBits();
    }
}
