package com.github.azeroth.game.networking.packet.guild;


public class GuildEventPresenceChange extends ServerPacket {
    public ObjectGuid UUID = ObjectGuid.EMPTY;
    public int virtualRealmAddress;
    public String name;
    public boolean mobile;
    public boolean loggedOn;

    public GuildEventPresenceChange() {
        super(ServerOpcode.GuildEventPresenceChange);
    }

    @Override
    public void write() {
        this.writeGuid(UUID);
        this.writeInt32(virtualRealmAddress);

        this.writeBits(name.getBytes().length, 6);
        this.writeBit(loggedOn);
        this.writeBit(mobile);

        this.writeString(name);
    }
}
