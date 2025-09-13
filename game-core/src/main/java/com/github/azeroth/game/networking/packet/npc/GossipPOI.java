package com.github.azeroth.game.networking.packet.npc;

import com.github.azeroth.game.networking.ServerPacket;

public class GossipPOI extends ServerPacket {
    public int id;
    public int flags;
    public Vector3 pos;
    public int icon;
    public int importance;
    public int WMOGroupID;
    public String name;

    public GossipPOI() {
        super(ServerOpcode.GossipPoi);
    }

    @Override
    public void write() {
        this.writeInt32(id);
        this.writeVector3(pos);
        this.writeInt32(icon);
        this.writeInt32(importance);
        this.writeInt32(WMOGroupID);
        this.writeBits(flags, 14);
        this.writeBits(name.getBytes().length, 6);
        this.flushBits();
        this.writeString(name);
    }
}
