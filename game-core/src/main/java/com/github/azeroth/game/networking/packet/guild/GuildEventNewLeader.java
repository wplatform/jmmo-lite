package com.github.azeroth.game.networking.packet.guild;


public class GuildEventNewLeader extends ServerPacket {
    public ObjectGuid newLeaderGUID = ObjectGuid.EMPTY;
    public String newLeaderName;
    public int newLeaderVirtualRealmAddress;
    public ObjectGuid oldLeaderGUID = ObjectGuid.EMPTY;
    public String oldLeaderName = "";
    public int oldLeaderVirtualRealmAddress;
    public boolean selfPromoted;

    public GuildEventNewLeader() {
        super(ServerOpcode.GuildEventNewLeader);
    }

    @Override
    public void write() {
        this.writeBit(selfPromoted);
        this.writeBits(oldLeaderName.getBytes().length, 6);
        this.writeBits(newLeaderName.getBytes().length, 6);

        this.writeGuid(oldLeaderGUID);
        this.writeInt32(oldLeaderVirtualRealmAddress);
        this.writeGuid(newLeaderGUID);
        this.writeInt32(newLeaderVirtualRealmAddress);

        this.writeString(oldLeaderName);
        this.writeString(newLeaderName);
    }
}
