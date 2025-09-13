package com.github.azeroth.game.networking.packet.misc;


public class DeathReleaseLoc extends ServerPacket {
    public int mapID;
    public Worldlocation loc;

    public DeathReleaseLoc() {
        super(ServerOpcode.DeathReleaseLoc);
    }

    @Override
    public void write() {
        this.writeInt32(mapID);
        this.writeXYZ(loc);
    }
}
