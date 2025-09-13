package com.github.azeroth.game.networking.packet.guild;


import com.github.azeroth.game.networking.ServerPacket;

public class GuildEventPlayerLeft extends ServerPacket {
    public ObjectGuid leaverGUID = ObjectGuid.EMPTY;
    public String leaverName;

    public int leaverVirtualRealmAddress;
    public ObjectGuid removerGUID = ObjectGuid.EMPTY;
    public String removerName;

    public int removerVirtualRealmAddress;
    public boolean removed;

    public GuildEventPlayerLeft() {
        super(ServerOpcode.GuildEventPlayerLeft);
    }

    @Override
    public void write() {
        this.writeBit(removed);
        this.writeBits(leaverName.getBytes().length, 6);

        if (removed) {
            this.writeBits(removerName.getBytes().length, 6);
            this.writeGuid(removerGUID);
            this.writeInt32(removerVirtualRealmAddress);
            this.writeString(removerName);
        }

        this.writeGuid(leaverGUID);
        this.writeInt32(leaverVirtualRealmAddress);
        this.writeString(leaverName);
    }
}
