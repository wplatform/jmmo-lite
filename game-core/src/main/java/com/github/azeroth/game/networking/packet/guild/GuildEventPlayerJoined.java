package com.github.azeroth.game.networking.packet.guild;


public class GuildEventPlayerJoined extends ServerPacket {
    public ObjectGuid UUID = ObjectGuid.EMPTY;
    public String name;
    public int virtualRealmAddress;

    public GuildEventPlayerJoined() {
        super(ServerOpcode.GuildEventPlayerJoined);
    }

    @Override
    public void write() {
        this.writeGuid(UUID);
        this.writeInt32(virtualRealmAddress);

        this.writeBits(name.getBytes().length, 6);
        this.writeString(name);
    }
}
